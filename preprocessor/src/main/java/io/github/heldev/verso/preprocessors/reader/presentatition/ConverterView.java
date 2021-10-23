package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.TypeName;

public class ConverterView {
	private final TypeName clazz;
	private final String method;
	private final TypeName targetType;

	public ConverterView(TypeName clazz, String method, TypeName targetType) {
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
