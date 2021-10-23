package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.twitter.scrooge.ast.Field;
import com.twitter.scrooge.ast.NamedType;
import com.twitter.scrooge.ast.RHS;
import com.twitter.scrooge.ast.StructLike;
import com.twitter.scrooge.ast.TBinary$;
import com.twitter.scrooge.ast.TString$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.interfaces.IngressField;
import io.github.heldev.verso.interfaces.VersoIngress;
import io.github.heldev.verso.preprocessors.reader.presentatition.FieldView;
import io.github.heldev.verso.preprocessors.reader.presentatition.View;
import io.github.heldev.verso.stronglytyped.Converters;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.typesIn;

public class ReaderBuilder {
	private final Converters converters;
	private final Filer filer;
	private final Elements elementUtils;
	private final Types typeUtils;
	private final ThriftParser thriftParser;


	public ReaderBuilder(
			Converters converters,
			ProcessingEnvironment processingEnvironment,
			ThriftParser thriftParser) {
		this.converters = converters;
		this.elementUtils = processingEnvironment.getElementUtils();
		this.typeUtils = processingEnvironment.getTypeUtils();
		filer = processingEnvironment.getFiler();
		this.thriftParser = thriftParser;
	}

	public Set<View> generateReaders(RoundEnvironment roundEnv) {

		Map<Boolean, List<TypeElement>> collect = typesIn(roundEnv.getElementsAnnotatedWith(VersoIngress.class)).stream()
				.filter(type -> type.getKind().isClass())
				.collect(partitioningBy(type -> type.getKind().isClass()));

		List<TypeElement> nonClasses = collect.getOrDefault(false, emptyList());
		if (nonClasses.isEmpty()) {

			return collect.getOrDefault(true, emptyList()).stream()
					.map(this::generateReader)
					.collect(toSet());

		} else {
			throw new RuntimeException("Only classes are supported at this moment, offenders: " + nonClasses);
		}
	}

	public View generateReader(TypeElement clazz) {

		return new View(
				TypeName.get(clazz.asType()),
				clazz.getSimpleName().toString() + "$$IngressReader",
				elementUtils.getPackageOf(clazz).toString(),
				buildFields(clazz));
	}

	private List<FieldView> buildFields(TypeElement clazz) {
		ExecutableElement constructor = getVersoConstructor(clazz);
		StructLike thriftStruct = getThriftStruct(clazz);

		return constructor
				.getParameters().stream()
				.map(parameter -> buildField(thriftStruct, parameter))
				.collect(toList());
	}

	private FieldView buildField(StructLike thriftStruct, VariableElement parameter) {
		Field thriftField = getThriftField(thriftStruct, parameter);

		if (! isOptional(parameter) && (thriftField.requiredness().isOptional() || thriftField.requiredness().isDefault() && defaultsToNull(thriftField))) {
			throw new RuntimeException("thrift definition with possible null " + thriftField + ", " + parameter);
		} else {
//					converters.canDo(new Conversion(thriftField, ConcreteType.of(parameter.asType()).stripOption.boxIfNeeded)).orElseThrow());

			return new FieldView(
					thriftField.index(),
					parameter.getSimpleName().toString(),
					getBuilderFieldType(thriftField, parameter),
					thriftField.requiredness().isRequired(),
					isOptional(parameter),
					null); //todo finish
		}
	}

	private Boolean isOptionalOrDefaultsToNull(Field thriftField, VariableElement parameter) {

		return isOptional(parameter)
				|| thriftField.requiredness().isOptional()
				|| (thriftField.requiredness().isDefault() && defaultsToNull(thriftField));
	}

	private StructLike getThriftStruct(TypeElement clazz) {
		VersoIngress annotation = clazz.getAnnotation(VersoIngress.class);

		return thriftParser.parseFile(annotation.thriftFile())
				.structs()
				.find(struct -> struct.originalName().equals(annotation.structName()))
				.get(); //todo safety
	}

	private ExecutableElement getVersoConstructor(TypeElement clazz) {
		//todo validation, multiple constructors and safety
		return constructorsIn(clazz.getEnclosedElements()).get(0);
	}

	private boolean defaultsToNull(Field thriftField) {
		try {
			Option<RHS> aDefault = (Option<RHS>) thriftField.getClass().getDeclaredMethod("default").invoke(thriftField);

			boolean defaultsToNull = thriftField.requiredness().isDefault() && aDefault.isEmpty() && (
					asList(TBinary$.MODULE$, TString$.MODULE$).contains(thriftField.fieldType())
							|| thriftField.fieldType() instanceof NamedType);

			return defaultsToNull;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private TypeName getBuilderFieldType(Field thriftField, VariableElement parameter) {
		TypeMirror parameterType = parameter.asType();

		if (parameterType.getKind().isPrimitive() && ! thriftField.requiredness().isDefault()) {
			return TypeName.get(parameterType).box();
		} else if (typeUtils.isAssignable(typeUtils.erasure(parameterType), typeUtils.getDeclaredType(elementUtils.getTypeElement(Optional.class.getName())))) {
			//todo vavr, guava, custom optional
			return ((ParameterizedTypeName) TypeName.get(parameterType)).typeArguments.get(0);
		} else {
			return TypeName.get(parameterType);
		}
	}

	private Boolean isOptional(VariableElement parameter) {

		return typeUtils.isSameType(
				typeUtils.erasure(parameter.asType()),
				typeUtils.getDeclaredType(elementUtils.getTypeElement(Optional.class.getCanonicalName())));
	}

	private Field getThriftField(StructLike thriftStruct, VariableElement parameter) {
		//todo annotation safety
		int id = parameter.getAnnotation(IngressField.class).id();

		return thriftStruct.fields().find(field -> field.index() == id).get();
	}

}
