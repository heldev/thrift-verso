package io.github.heldev.verso.preprocessors.reader.presentatition.defaults;

import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;

import java.util.Optional;

public class DefaultStringView implements DefaultView {

	public static DefaultStringView of(String value) {
		return new DefaultStringView(value, Optional.empty());
	}

	public static DefaultStringView of(String value, ConverterView converter) {
		return new DefaultStringView(value, Optional.of(converter));
	}

	private final String value;
	private final Optional<ConverterView> converter;

	private DefaultStringView(String value, Optional<ConverterView> converter) {
		this.value = value;
		this.converter = converter;
	}

	public String value() {
		return value;
	}

	public Optional<ConverterView> converter() {
		return converter;
	}
}
