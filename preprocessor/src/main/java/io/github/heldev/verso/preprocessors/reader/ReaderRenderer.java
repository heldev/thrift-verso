package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;
import io.github.heldev.verso.preprocessors.reader.presentatition.FieldView;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftBasicType;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftListType;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftSetType;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftTypeView;
import io.github.heldev.verso.preprocessors.reader.presentatition.View;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ReaderRenderer {

	public JavaFile render(View view) {

		return JavaFile.builder(view.readerPackage(), buildType(view))
				.indent("\t")
				.build();
	}

	private TypeSpec buildType(View view) {

		return TypeSpec.classBuilder(view.readerName())
				.addModifiers(PUBLIC)
				.addJavadoc("Note: The classe uses null's internally for performance reason")
				.addMethod(buildMethod(view))
				.addType(buildBuilder(view))
				.build();
	}

	private MethodSpec buildMethod(View view) {

		return MethodSpec.methodBuilder("read")
				.addModifiers(PUBLIC)
				.returns(view.dtoType())
				.addParameter(TProtocol.class, "protocol")
				.addException(TException.class)
				.addStatement("var builder = new Builder()")
				.addStatement("protocol.readStructBegin()")
				.beginControlFlow("while(true)")
				.addCode(buildReadLoopBlock(view.fields()))
				.endControlFlow()
				.addStatement("protocol.readStructEnd()")
				.addStatement("return builder.build()")
				.build();
	}

	private CodeBlock buildReadLoopBlock(List<FieldView> fields) {

		return CodeBlock.builder()
				.addStatement("var field = protocol.readFieldBegin()")
				.beginControlFlow("if (field.type == $T.STOP)", TType.class)
				.addStatement("protocol.readFieldEnd()")
				.addStatement("break")
				.endControlFlow()
				.beginControlFlow("switch(field.id)")
				.add(buildReadFieldsBlock(fields))
				.endControlFlow()
				.addStatement("protocol.readFieldEnd()")
				.build();
	}

	private CodeBlock buildReadFieldsBlock(List<FieldView> fields) {

		return fields.stream()
				.map(this::buildReadFieldCase)
				.collect(CodeBlock.joining("\n"));
	}

	private CodeBlock buildReadFieldCase(FieldView field) {
		ThriftTypeView type = field.thriftType();
		CodeAndResult codeAndResult = buildReadField(type, 0);

		return CodeBlock.builder()
				.beginControlFlow("case $L:", field.id()) //todo add constants
				.add(codeAndResult.code().orElse(CodeBlock.of("")))
				.add("builder.$L = ", field.name())
				.add(codeAndResult.result())
				.add(";\n")
				.addStatement("break")
				.endControlFlow()
				.build();
	}

	private CodeAndResult buildReadField(ThriftTypeView type, Integer level) {

		return buildReadField0(type, level)
				.mapResult(value -> wrapInConverterIfNeeded(value, type.converter()));
	}

	private CodeAndResult buildReadField0(ThriftTypeView type, Integer level) {

		if (type instanceof ThriftBasicType) {
			ThriftBasicType basicType = (ThriftBasicType) type;
			return new CodeAndResult(CodeBlock.of("protocol.read$L()", basicType.methodRoot()));
		} else if (type instanceof ThriftListType) {
			ThriftListType listType = (ThriftListType) type;

			return buildReadList(listType, level);
		} else if (type instanceof ThriftSetType) {
			ThriftSetType setType = (ThriftSetType) type;

			return buildReadSet(setType, level);
		} else {
			throw new RuntimeException("wip");
		}
	}

	private CodeAndResult buildReadList(ThriftListType type, Integer level) {
		CodeAndResult elementsCodeAndResult = buildReadField(type.subtype(), level + 1);

		CodeBlock code = CodeBlock.builder()
				.addStatement("var remainingElements$L = protocol.readListBegin().size", level)
				.addStatement("$1T<$2T> collection$3L = new $4T<>(remainingElements$3L)", List.class, type.subtype().targetType(), level, ArrayList.class)
				.beginControlFlow("for(; 0 < remainingElements$1L; remainingElements$1L--)", level)
				.add(elementsCodeAndResult.code().orElse(CodeBlock.of("")))
				.add("collection$L.add(", level)
				.add(elementsCodeAndResult.result())
				.add(");\n")
				.endControlFlow()
				.addStatement("protocol.readListEnd()")
				.build();

		CodeBlock result = CodeBlock.of("collection$L", level);

		return new CodeAndResult(code, result);
	}

	private CodeAndResult buildReadSet(ThriftSetType type, Integer level) {
		CodeAndResult elementsCodeAndResult = buildReadField(type.subtype(), level + 1);

		CodeBlock code = CodeBlock.builder()
				.addStatement("var remainingElements$L = protocol.readSetBegin().size", level)
				.addStatement("$T<$T> collection$L = new $T<>()", Set.class, type.subtype().targetType(), level, HashSet.class)
				.beginControlFlow("for(; 0 < remainingElements$1L; remainingElements$1L--)", level)
				.add(elementsCodeAndResult.code().orElse(CodeBlock.of("")))
				.add("collection$L.add(", level)
				.add(elementsCodeAndResult.result())
				.add(");\n")
				.endControlFlow()
				.addStatement("protocol.readSetEnd()")
				.build();

		CodeBlock result = CodeBlock.of("collection$L", level);

		return new CodeAndResult(code, result);
	}

	//todo move to the CodeAndResult class
	private CodeBlock wrapInConverterIfNeeded(
			CodeBlock value,
			@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ConverterView> converterView) {

		return converterView
				.map(converter -> wrapInConverter(value, converter))
				.orElse(value);
	}

	private CodeBlock wrapInConverter(CodeBlock value, ConverterView converter) {

		return CodeBlock.builder()
				.add("$T.$L(", converter.clazz(), converter.method())
				.add(value)
				.add(")")
				.build();
	}

	private TypeSpec buildBuilder(View view) {
		MethodSpec validationMethod = buildValidationMethod(view);

		return TypeSpec.classBuilder("Builder") //todo name conflicts
				.addFields(view.fields().stream().map(this::buildField).collect(toList()))
				.addMethod(buildBuilderMethod(view, validationMethod))
				.addMethod(validationMethod)
				.build();
	}

	private FieldSpec buildField(FieldView fieldView) {

		return FieldSpec.builder(fieldView.builderFieldType(), fieldView.name())
				.build();
	}

	private MethodSpec buildBuilderMethod(View view, MethodSpec validationMethod) {

		return MethodSpec.methodBuilder("build")
				.returns(view.dtoType())
				.addStatement("$N()", validationMethod)
				.addCode("return new $T(", view.dtoType())
				.addCode(buildConstructorArguments(view))
				.addCode(");")
				.build();
	}

	private CodeBlock buildConstructorArguments(View view) {

		return view.fields()
				.stream()
				.map(this::buildConstructorArgument)
				.collect(CodeBlock.joining(", "));
	}

	private CodeBlock buildConstructorArgument(FieldView argument) {
		//todo handle other types of optional

		return argument.wrapInOptional()
				? CodeBlock.of("$T.ofNullable($L)", Optional.class, argument.name())
				: CodeBlock.of("$L", argument.name());
	}

	private MethodSpec buildValidationMethod(View view) {

		return MethodSpec.methodBuilder("throwIfInvalid")
				.addStatement("var missingRequiredFields = new $T<>()", ArrayList.class)
				.beginControlFlow("if (! missingRequiredFields.isEmpty())")
				.addCode(buildFieldChecks(view.fields()))
				.addStatement("throw new RuntimeException(\"Missing required fields: \" + missingRequiredFields)")
				.endControlFlow()
				.build();
	}

	private CodeBlock buildFieldChecks(List<FieldView> fields) {

		return fields.stream()
				.filter(FieldView::throwIfNotProvided)
				.map(this::buildFieldCheck)
				.collect(CodeBlock.joining("\n"));
	}

	private CodeBlock buildFieldCheck(FieldView field) {

		return CodeBlock.builder()
				.beginControlFlow("if ($L == null)", field.name())
				.addStatement("missingRequiredFields.add($S)", field.name())
				.endControlFlow()
				.build();
	}
}
