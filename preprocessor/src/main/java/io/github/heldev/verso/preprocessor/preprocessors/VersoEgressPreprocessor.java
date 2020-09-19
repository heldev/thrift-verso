package io.github.heldev.verso.preprocessor.preprocessors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.twitter.scrooge.ast.StructLike;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.preprocessor.EgressField;
import io.github.heldev.verso.preprocessor.VersoEgress;
import io.github.heldev.verso.preprocessor.VersoWriter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static javax.lang.model.util.ElementFilter.typesIn;

public class VersoEgressPreprocessor {

	private final ProcessingEnvironment processingEnvironment;
	private final Filer filer;
	private final Elements elementUtils;
	private final Types typeUtils;
	private final ThriftParser thriftParser;

	public VersoEgressPreprocessor(ProcessingEnvironment processingEnvironment, ThriftParser thriftParser) {
		this.processingEnvironment = processingEnvironment;
		this.elementUtils = processingEnvironment.getElementUtils();
		this.typeUtils = processingEnvironment.getTypeUtils();
		filer = processingEnvironment.getFiler();
		this.thriftParser = thriftParser;
	}

	public void generateWriters(RoundEnvironment roundEnv) {
		for (TypeElement clazz : typesIn(roundEnv.getElementsAnnotatedWith(VersoEgress.class))) {
			if (clazz.getKind().isClass()) {
				try {
					String packageName = elementUtils.getPackageOf(clazz).toString();
					TypeSpec typeSpec = getTypeSpec(clazz, getThriftStruct(clazz));

					JavaFile.builder(packageName, typeSpec)
							.indent("\t")
							.build()
							.writeTo(filer);

				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			} else {
				throw new UnsupportedOperationException("Only classes are suppported for now");
			}
		}
	}

	private StructLike getThriftStruct(TypeElement clazz) {
		var annotation = clazz.getAnnotation(VersoEgress.class);

		var thrift = thriftParser.parseFile(annotation.thriftFile())
				.structs()
				.find(struct -> struct.originalName().equals(annotation.structName()))
				.get(); //todo safety
		return thrift;
	}

	private TypeSpec getTypeSpec(TypeElement clazz, StructLike thriftStruct) {

		var parameterizedVersoWriter = ParameterizedTypeName.get(
				ClassName.get(VersoWriter.class),
				TypeName.get(clazz.asType()));

		return TypeSpec.classBuilder(clazz.getSimpleName().toString() + "$$EgressWriter")
				.addSuperinterface(parameterizedVersoWriter)
				.addMethod(buildWriterMethod(clazz, thriftStruct))
				.build();
	}

	private MethodSpec buildWriterMethod(TypeElement clazz, StructLike thriftStruct) {

		var fields = methodsIn(clazz.getEnclosedElements()).stream()
                .filter(method -> method.getAnnotation(EgressField.class) != null)
                .map(method -> {
			var annotation = method.getAnnotation(EgressField.class);

//			var thriftType = thriftStruct.fields()
//					.find(field -> annotation.id() == field.index())
//					.map(field -> {
//						if (field.fieldType() instanceof TBool$) {
//							return field.fieldType().
//
//						} else if (field.fieldType() instanceof TString$) {
//
//						} else {
//
//						}
//					})
//					.get(); //todo safety
		   var thriftType = getThriftType(method);

			CodeBlock fieldWriter;
			if (thriftType instanceof ThriftPrimitive) {
				fieldWriter = CodeBlock.builder()
						.addStatement("protocol.write$L(object.$L())", thriftType.getType(), method.getSimpleName())
						.build();
			}
			else if (thriftType instanceof ThriftContainer) {
				var visitor = new SimpleTypeVisitor14<TypeMirror, Void>() {
					@Override
					public TypeMirror visitDeclared(DeclaredType t, Void o) {
						return t.getTypeArguments().get(0);
					}
				};

				var elementType = getThriftType(method.getReturnType().accept(visitor, null));
				fieldWriter = CodeBlock.builder()
						.addStatement("protocol.write$LBegin(new $T($T.$L, object.$L().size()))", thriftType.getType(),
								TList.class, TType.class, elementType.getType().toUpperCase(), method.getSimpleName())
						.beginControlFlow("for(var element: object.$L())", method.getSimpleName())
						.addStatement("//todo recursive")
						.endControlFlow()
						.addStatement("protocol.write$LEnd()", thriftType.getType()).build();
			}
			else {
				fieldWriter = CodeBlock.of("new $L$$$$EgressWriter().write(object.$L(), protocol)",
						ClassName.get(method.getReturnType()), method.getSimpleName());
			}

			return CodeBlock.builder()
					.addStatement("protocol.writeFieldBegin(new $T($S, $T.$L, (short) $L))", TField.class,
							getThriftName(method.getSimpleName()), TType.class, getTtype(method), annotation.id())
					.add(fieldWriter).addStatement("protocol.writeFieldEnd()").build();
		}).collect(CodeBlock.joining("\n"));

		return MethodSpec.methodBuilder("write").addAnnotation(Override.class).addModifiers(PUBLIC)
				.addParameter(TypeName.get(clazz.asType()), "object").addParameter(TProtocol.class, "protocol")
				.addException(TException.class)
				.addStatement("protocol.writeStructBegin(new $T($S))", TStruct.class, clazz.getSimpleName())
				.addCode("\n").addCode(fields).addCode("\n").addStatement("protocol.writeFieldStop()")
				.addStatement("protocol.writeStructEnd()").build();
	}

	private String getThriftName(Name accessorName) {
		// todo: replace with real thrift name
		return accessorName.toString().toLowerCase().substring(3);
	}

	private ThriftType getThriftType(ExecutableElement method) {
     	return getThriftType(method.getReturnType());
    }

	private ThriftType getThriftType(TypeMirror type) {

		return switch (type.getKind()) {
		case BOOLEAN -> new ThriftPrimitive("Bool");
		case BYTE -> new ThriftPrimitive("Byte");
		case SHORT -> new ThriftPrimitive("I16");
		case INT -> new ThriftPrimitive("I32");
		case LONG -> new ThriftPrimitive("I64");
		case FLOAT, DOUBLE -> new ThriftPrimitive("Double");
		case VOID -> throw new RuntimeException("accessor shouldn't return void");

		case CHAR, NULL, ARRAY, TYPEVAR, WILDCARD, OTHER, UNION, INTERSECTION -> throw new UnsupportedOperationException(
                type.toString());

		case DECLARED -> {

			var charSequence = elementUtils.getTypeElement(CharSequence.class.getCanonicalName()).asType();
			var rawSet = typeUtils.erasure(elementUtils.getTypeElement(Set.class.getCanonicalName()).asType());
			var rawMap = typeUtils.erasure(elementUtils.getTypeElement(Map.class.getCanonicalName()).asType());
			var rawCollection = typeUtils
					.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType());

			// todo handle boxed
			if (typeUtils.isAssignable(type, charSequence)) {
				yield new ThriftPrimitive("String");
			}
			else if (typeUtils.isAssignable(typeUtils.erasure(type), rawSet)) {
				yield new ThriftContainer("Set");
			}
			else if (typeUtils.isAssignable(typeUtils.erasure(type), rawMap)) {
				yield new ThriftContainer("Map");
			}
			else if (typeUtils.isAssignable(typeUtils.erasure(type), rawCollection)) {
				yield new ThriftContainer("List");
			}
			else {
				yield new ThriftStruct("Struct");
			}
		}

		case ERROR -> throw new RuntimeException("ERROR return type, something is wrong");

		default -> throw new RuntimeException("deafult branch");
		};
	}

	@Deprecated
	private String getTtype(ExecutableElement method) {
		var returnType = method.getReturnType();

		return switch (returnType.getKind()) {
		case BOOLEAN -> "BOOL";
		case BYTE -> "BYTE";
		case SHORT -> "I16";
		case INT -> "I32";
		case LONG -> "I64";
		case FLOAT, DOUBLE -> "DOUBLE";
		case VOID -> throw new RuntimeException("accessor shouldn't return void");

		case CHAR, NULL, ARRAY, TYPEVAR, WILDCARD, OTHER, UNION, INTERSECTION -> throw new UnsupportedOperationException(
				returnType.toString());

		case DECLARED -> {

			var rawSet = typeUtils.erasure(elementUtils.getTypeElement(Set.class.getCanonicalName()).asType());
			var rawMap = typeUtils.erasure(elementUtils.getTypeElement(Map.class.getCanonicalName()).asType());
			var rawCollection = typeUtils
					.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType());

			// todo handle boxed
			if (typeUtils.isAssignable(typeUtils.erasure(returnType), rawSet)) {
				yield "SET";
			}
			else if (typeUtils.isAssignable(typeUtils.erasure(returnType), rawMap)) {
				throw new UnsupportedOperationException("MAP is todo");// yield "MAP";
			}
			else if (typeUtils.isAssignable(typeUtils.erasure(returnType), rawCollection)) {
				yield "LIST";
			}
			else {
				yield "STRUCT";
			}
		}

		case ERROR -> throw new RuntimeException("ERROR return type, something is wrong");

		default -> throw new RuntimeException("deafult branch");
		};
	}

}

interface ThriftType {

	String getType();

}

final class ThriftPrimitive implements ThriftType {

	private final String type;

	public ThriftPrimitive(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

}

final class ThriftContainer implements ThriftType {

	private final String type;

	public ThriftContainer(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

}

final class ThriftStruct implements ThriftType {

	private final String type;

	public ThriftStruct(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

}