package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class CodeAndResult {
	private final Optional<CodeBlock> code;
	private final CodeBlock result;

	public CodeAndResult(CodeBlock result) {
		this(Optional.empty(), result);
	}

	CodeAndResult(CodeBlock code, CodeBlock result) {
		this.code = Optional.of(code);
		this.result = result;
	}

	private CodeAndResult(Optional<CodeBlock> code, CodeBlock result) {
		this.code = code;
		this.result = result;
	}

	public Optional<CodeBlock> code() {
		return code;
	}

	public CodeBlock result() {
		return result;
	}

	public CodeAndResult mapResult(Function<CodeBlock, CodeBlock> mapper) {
		return new CodeAndResult(code, mapper.apply(result));
	}
}
