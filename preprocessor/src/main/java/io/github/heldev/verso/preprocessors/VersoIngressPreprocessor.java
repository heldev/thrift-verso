package io.github.heldev.verso.preprocessors;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.twitter.scrooge.ast.Field;
import com.twitter.scrooge.ast.NamedType;
import com.twitter.scrooge.ast.RHS;
import com.twitter.scrooge.ast.StructLike;
import com.twitter.scrooge.ast.TBinary$;
import com.twitter.scrooge.ast.TString$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.interfaces.IngressField;
import io.github.heldev.verso.interfaces.VersoIngress;
import io.github.heldev.verso.stronglytyped.Converters;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import scala.Option;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.typesIn;

public class VersoIngressPreprocessor {
	private final Converters converters;
	private final Filer filer;
	private final Elements elementUtils;
	private final Types typeUtils;
	private final ThriftParser thriftParser;


	public VersoIngressPreprocessor(
			Converters converters,
			ProcessingEnvironment processingEnvironment,
			ThriftParser thriftParser) {
		this.converters = converters;
		this.elementUtils = processingEnvironment.getElementUtils();
		this.typeUtils = processingEnvironment.getTypeUtils();
		filer = processingEnvironment.getFiler();
		this.thriftParser = thriftParser;
	}

	public void generateReaders(RoundEnvironment roundEnv) {
		for (TypeElement type : typesIn(roundEnv.getElementsAnnotatedWith(VersoIngress.class))) {
			if (type.getKind().isClass()) {
				generateReader(type);
			} else {
				throw new RuntimeException("Only classes are supported at this moment");
			}
		}
	}

	private void generateReader(TypeElement clazz) {
		String packageName = elementUtils.getPackageOf(clazz).toString();
		TypeSpec typeSpec = buildTypeSpec(clazz, getThriftStruct(clazz));

		saveAsFile(packageName, typeSpec);
	}

	private StructLike getThriftStruct(TypeElement clazz) {
		VersoIngress annotation = clazz.getAnnotation(VersoIngress.class);

		return thriftParser.parseFile(annotation.thriftFile())
				.structs()
				.find(struct -> struct.originalName().equals(annotation.structName()))
				.get(); //todo safety
	}

	private TypeSpec buildTypeSpec(TypeElement clazz, StructLike thriftStruct) {
		ExecutableElement constructor = getVersoConstructor(clazz);
		TypeSpec builderType = buildBuilderType(constructor, thriftStruct);

		return TypeSpec.classBuilder(clazz.getSimpleName() + "$$IngressReader")
				.addMethod(buildReadMethod(clazz, thriftStruct))
				.addType(builderType)
//				.addMethod(buildReadFieldMethod())
//				.addType(buildBuilderClass())
				.build();
	}

	private ExecutableElement getVersoConstructor(TypeElement clazz) {
		//todo validation
		return constructorsIn(clazz.getEnclosedElements()).get(0);
	}

	private TypeSpec buildBuilderType(ExecutableElement constructor, StructLike thriftStruct) {
		Iterable<FieldSpec> builderFields = buildBuilderFields(constructor, thriftStruct);

		List<CodeBlock> blocks = constructor.getParameters().stream().flatMap(parameter -> {
			int id = parameter.getAnnotation(IngressField.class).id();
			Field thriftField = thriftStruct.fields().find(field -> field.index() == id).get(); //todo

			try {
				Option<RHS> aDefault = (Option<RHS>) thriftField.getClass().getDeclaredMethod("default").invoke(thriftField);

				boolean defaultsToNull = thriftField.requiredness().isDefault() && aDefault.isEmpty() && (
						asList(TBinary$.MODULE$, TString$.MODULE$).contains(thriftField.fieldType())
								|| thriftField.fieldType() instanceof NamedType);

				if (thriftField.requiredness().isRequired() || defaultsToNull) {
					return Stream.of(CodeBlock.of("$1L == null ? Stream.of($1S) : Stream.empty()", parameter.getSimpleName()));
				} else {
					return Stream.empty();
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}).collect(toList());


		MethodSpec throwIfInvalidMethod = MethodSpec.methodBuilder("throwIfInvalid")
				.addCode("var missingRequiredFields = $T.<Stream<String>>of(\n\t\t", Stream.class)
				.addCode(CodeBlock.join(blocks, ",\n\t\t"))
				.addCode("\n).flatMap($T.identity()).collect($T.joining(\", \"));\n\n", Function.class, Collectors.class)
				.beginControlFlow("if (! missingRequiredFields.isBlank())")
				.addStatement("throw new RuntimeException(\"Missing required fields: \" + missingRequiredFields)")
				.endControlFlow()
				.build();


		return TypeSpec.classBuilder("Builder")
				.addModifiers(PRIVATE, STATIC)
				.addFields(builderFields)
				.addMethod(MethodSpec.methodBuilder("build")
						.returns(TypeName.get(constructor.getEnclosingElement().asType()))
						.addStatement("$N()", throwIfInvalidMethod)
						.addCode("\n")
						.addCode("return new $L", constructor.getEnclosingElement().getSimpleName())
						.addCode(constructor.getParameters().stream().map(parameter -> {
							int id = parameter.getAnnotation(IngressField.class).id();
							Field thriftField = thriftStruct.fields().find(field -> field.index() == id).get(); //todo

							boolean isOptional = typeUtils.isSameType(
									typeUtils.erasure(parameter.asType()),
									typeUtils.getDeclaredType(elementUtils.getTypeElement(Optional.class.getCanonicalName())));


							return isOptional
									? CodeBlock.of("$T.ofNullable($L)", Optional.class, parameter.getSimpleName())
									: CodeBlock.of(parameter.getSimpleName().toString());
						}).collect(CodeBlock.joining(",\n\t\t", "(\n\t\t", ");")))
						.build())
				.addMethod(throwIfInvalidMethod)
				.build();
	}

	private Iterable<FieldSpec> buildBuilderFields(ExecutableElement constructor, StructLike thriftStruct) {

		return constructor.getParameters()
				.stream()
				.map(parameter -> buildBuilderField(thriftStruct, parameter))
				.collect(toList());
	}

	private FieldSpec buildBuilderField(StructLike thriftStruct, VariableElement parameter) {
		TypeName builderFieldType = getBuilderFieldType(thriftStruct, parameter);

		return FieldSpec.builder(builderFieldType, parameter.getSimpleName().toString())
				.build();
	}

	private TypeName getBuilderFieldType(StructLike thriftStruct, VariableElement parameter) {
		//todo annotation safety
		Field scroogeField = thriftStruct.fields().find(field -> field.index() == parameter.getAnnotation(IngressField.class).id()).get();

		TypeMirror parameterType = parameter.asType();
		if (scroogeField.requiredness().isDefault() && parameterType.getKind().isPrimitive()) {
			return TypeName.get(parameterType).box();
		} else if (typeUtils.isAssignable(typeUtils.erasure(parameterType), typeUtils.getDeclaredType(elementUtils.getTypeElement(Optional.class.getName())))) {
			//todo vavr, guava, custom optional
			return ((ParameterizedTypeName) TypeName.get(parameterType)).typeArguments.get(0);
		} else {
			return TypeName.get(parameterType);
		}
	}

	private MethodSpec buildReadMethod(TypeElement clazz, StructLike thriftStruct) {

		return MethodSpec.methodBuilder("read")
				.addModifiers(PUBLIC)
				.returns(TypeName.get(clazz.asType()))
				.addParameter(TProtocol.class, "protocol")
				.addException(TException.class)
				.addStatement("var builder = new Builder()") //todo possible name collision
				.addStatement("//todo builder fields population")
				.addStatement("return builder.build()") //todo convert exception
				.build();
	}

	private void saveAsFile(String packageName, TypeSpec typeSpec) {
		try {

			JavaFile.builder(packageName, typeSpec)
					.indent("\t")
					.build()
					.writeTo(filer);

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
