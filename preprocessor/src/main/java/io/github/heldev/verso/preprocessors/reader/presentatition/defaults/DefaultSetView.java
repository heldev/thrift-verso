package io.github.heldev.verso.preprocessors.reader.presentatition.defaults;

import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DefaultSetView implements DefaultView {

	public static DefaultSetView of(Set<DefaultView> values) {
		return new DefaultSetView(values, Optional.empty());
	}

	public static DefaultSetView of(Set<DefaultView> values, ConverterView converter) {
		return new DefaultSetView(values, Optional.of(converter));
	}

	private final Set<DefaultView> values;
	private final Optional<ConverterView> converter;

	private DefaultSetView(Set<DefaultView> values, Optional<ConverterView> converter) {
		this.values = values;
		this.converter = converter;
	}

	public Set<DefaultView> values() {
		return values;
	}

	public Optional<ConverterView> converter() {
		return converter;
	}
}
