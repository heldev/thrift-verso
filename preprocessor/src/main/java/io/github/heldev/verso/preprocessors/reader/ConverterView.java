package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.TypeName;

class ConverterView {
	private final TypeName clazz;
	private final String method;
	private final TypeName targetType;

	ConverterView(TypeName clazz, String method, TypeName targetType) {
		this.clazz = clazz;
		this.method = method;
		this.targetType = targetType;
	}

	public TypeName clazz() {
		return clazz;
	}

	public String method() {
		return method;
	}

	public TypeName targetType() {
		return targetType;
	}
}
