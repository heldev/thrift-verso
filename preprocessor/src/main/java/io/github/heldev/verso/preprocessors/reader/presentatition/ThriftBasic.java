package io.github.heldev.verso.preprocessors.reader.presentatition;

public enum ThriftBasic {

	STRING("String"),
	I32("I32");

	private final String methodSuffix;

	ThriftBasic(String methodSuffix) {
		this.methodSuffix = methodSuffix;
	}

	public String methodSuffix() {
		return methodSuffix;
	}
}
