package io.github.heldev.verso.preprocessors.reader.presentatition.defaults;

import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;

import java.util.Optional;

public class DefaultI32View {

	public static DefaultI32View of(Integer value) {
		return new DefaultI32View(value, Optional.empty());
	}

	public static DefaultI32View of(Integer value, ConverterView converter) {
		return new DefaultI32View(value, Optional.of(converter));
	}

	private final Integer value;
	private final Optional<ConverterView> converter;

	private DefaultI32View(Integer value, Optional<ConverterView> converter) {
		this.value = value;
		this.converter = converter;
	}

	public Integer value() {
		return value;
	}

	public Optional<ConverterView> converter() {
		return converter;
	}
}
