package io.github.heldev.verso.preprocessor.preprocessors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.twitter.scrooge.ast.BaseType;
import com.twitter.scrooge.ast.Field;
import com.twitter.scrooge.ast.FieldType;
import com.twitter.scrooge.ast.ListType;
import com.twitter.scrooge.ast.MapType;
import com.twitter.scrooge.ast.SetType;
import com.twitter.scrooge.ast.StructLike;
import com.twitter.scrooge.ast.StructType;
import com.twitter.scrooge.ast.TBinary$;
import com.twitter.scrooge.ast.TBool$;
import com.twitter.scrooge.ast.TByte$;
import com.twitter.scrooge.ast.TDouble$;
import com.twitter.scrooge.ast.TI16$;
import com.twitter.scrooge.ast.TI32$;
import com.twitter.scrooge.ast.TI64$;
import com.twitter.scrooge.ast.TString$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.preprocessor.EgressField;
import io.github.heldev.verso.preprocessor.VersoEgress;
import io.github.heldev.verso.preprocessor.VersoWriter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

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
				String packageName = elementUtils.getPackageOf(clazz).toString();
				TypeSpec typeSpec = buildTypeSpec(clazz, getThriftStruct(clazz));

				try {

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

		return thriftParser.parseFile(annotation.thriftFile())
				.structs()
				.find(struct -> struct.originalName().equals(annotation.structName()))
				.get(); //todo safety
	}

	private TypeSpec buildTypeSpec(TypeElement clazz, StructLike thriftStruct) {

		var parameterizedVersoWriter = ParameterizedTypeName.get(
				ClassName.get(VersoWriter.class),
				TypeName.get(clazz.asType()));

		return TypeSpec.classBuilder(clazz.getSimpleName() + "$$EgressWriter")
				.addSuperinterface(parameterizedVersoWriter)
				.addMethod(buildWriterMethod(clazz, thriftStruct))
				.build();
	}

	private MethodSpec buildWriterMethod(TypeElement clazz, StructLike scroogeStruct) {

		return MethodSpec.methodBuilder("write")
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.addParameter(TypeName.get(clazz.asType()), "object")
				.addParameter(TProtocol.class, "protocol")
				.addException(TException.class)
				.addStatement("protocol.writeStructBegin(new $T($S))", TStruct.class, clazz.getSimpleName()) //todo thrift name
				.addCode("\n")
				.addCode(buildFields(clazz, scroogeStruct))
				.addCode("\n")
				.addStatement("protocol.writeFieldStop()")
				.addStatement("protocol.writeStructEnd()")
				.build();
	}

	private CodeBlock buildFields(TypeElement clazz, StructLike scroogeStruct) {

		return methodsIn(clazz.getEnclosedElements()).stream()
                .filter(method -> method.getAnnotation(EgressField.class) != null)
                .map(method -> buildField(scroogeStruct, method))
				.collect(CodeBlock.joining("\n"));
	}

	private CodeBlock buildField(StructLike scroogeStruct, ExecutableElement method) {
		var annotation = method.getAnnotation(EgressField.class);
		var scroogeField = getScroogeField(scroogeStruct, annotation);

		return CodeBlock.builder()
				.addStatement("protocol.writeFieldBegin(new $T($S, $T.$L, (short) $L))",
						TField.class,
						scroogeField.originalName(),
						TType.class,
						getTtype(scroogeField.fieldType()).toUpperCase(),
						annotation.id())
				.add(buildValue(scroogeField.fieldType(), 0,"object." + method.getSimpleName() + "()"))
				.addStatement("protocol.writeFieldEnd()").build();
	}

	private CodeBlock buildValue(FieldType scroogeField, Integer level, String valueCode) {
		if (scroogeField instanceof BaseType) {
			return buildPrimitive((BaseType) scroogeField, valueCode);
		} else if (scroogeField instanceof ListType) {
			return buildList((ListType) scroogeField, level, valueCode);
		} else if (scroogeField instanceof SetType) {
			return buildSet((SetType) scroogeField, level, valueCode);
		} else if(scroogeField instanceof MapType) {
			return buildMap((MapType) scroogeField, level, valueCode);
		} else {
			return buildStruct(scroogeField, valueCode);
		}
	}

	private CodeBlock buildStruct(FieldType scroogeType, String valueCode) {
		return CodeBlock.of("new GlobalVersoWriter().write($S, protocol)", valueCode);
	}

	private CodeBlock buildList(ListType scroogeListType, Integer nestedLevel, String valueCode) {

		return CodeBlock.builder()
				.addStatement("protocol.writeListBegin(new $T($T.$L, $L.size()))",
						TList.class,
						TType.class,
						getTtype(scroogeListType).toUpperCase(),
						valueCode)
				.add(buildIterationOverContainer(scroogeListType.eltType(), nestedLevel, valueCode))
				.addStatement("protocol.writeListEnd()")
				.build();
	}

	private CodeBlock buildSet(SetType scroogeSetType, Integer nestedLevel, String valueCode) {

		return CodeBlock.builder()
				.addStatement("protocol.writeSetBegin(new $T($T.$L, $L.size()))",
						TSet.class,
						TType.class,
						getTtype(scroogeSetType).toUpperCase(),
						valueCode)
				.add(buildIterationOverContainer(scroogeSetType.eltType(), nestedLevel, valueCode))
				.addStatement("protocol.writeSetEnd()")
				.build();
	}

	private CodeBlock buildMap(MapType scroogeMapType, Integer nestedLevel, String valueCode) {

		return CodeBlock.builder()
				.addStatement("protocol.writeMapBegin(new $T($T.$L, TType.$L, $L.size()))",
						TMap.class,
						TType.class,
						getTtype(scroogeMapType.keyType()).toUpperCase(),
						getTtype(scroogeMapType.valueType()).toUpperCase(),
						valueCode)
				.beginControlFlow("for(var entry$L: $L.entrySet())", nestedLevel, valueCode)
				.add(buildValue(scroogeMapType.keyType(), nestedLevel + 1, "entry" + nestedLevel + ".getKey()"))
				.add(buildValue(scroogeMapType.valueType(), nestedLevel + 1, "entry" + nestedLevel + ".getValue()"))
				.endControlFlow()
				.addStatement("protocol.writeMapEnd()")
				.build();
	}

	private CodeBlock buildIterationOverContainer(FieldType scroogeElementType, Integer nestedLevel, String valueCode) {

		return CodeBlock.builder()
				.beginControlFlow("for(var element$L: $L)", nestedLevel, valueCode)
				.add(buildValue(scroogeElementType, nestedLevel + 1, "element" + nestedLevel))
				.endControlFlow()
				.build();
	}

	private CodeBlock buildPrimitive(BaseType scroogeType, String valueCode) {
//		return CodeBlock.of("protocol.write$L($L)", getTtype(scroogeType), valueCode);
		return CodeBlock.builder().addStatement("protocol.write$L($L)", getTtype(scroogeType), valueCode).build();
	}

	private Field getScroogeField(StructLike scroogeStruct, EgressField annotation) {

		return scroogeStruct.fields()
				.find(field -> annotation.id() == field.index())
				.get();  //todo safety
	}

	private String getTtype(FieldType scroogeType) {

		Map<BaseType, String> baseTypes = Map.of(
			TBool$.MODULE$, "Bool",
			TByte$.MODULE$, "Byte",
			TI16$.MODULE$, "I16",
			TI32$.MODULE$, "I32",
			TI64$.MODULE$, "I64",
			TDouble$.MODULE$, "Double",
			TString$.MODULE$, "String",
			TBinary$.MODULE$, "Binary"
		);

		if (baseTypes.containsKey(scroogeType)) {
			return baseTypes.get(scroogeType);
		} else if (scroogeType instanceof ListType) {
			return "List";
		} else if (scroogeType instanceof SetType) {
			return "Set";
		} else if (scroogeType instanceof MapType) {
			return "Map";
		} else if (scroogeType instanceof StructType) {
			return "Struct";
		} else {
			throw new RuntimeException("this ttype is not supported yet");
		}
		//todo enum and union
	}

}
