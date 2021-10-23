package io.github.heldev.verso.preprocessors.reader.presentatition.defaults;

import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;

import java.util.List;
import java.util.Optional;

public class DefaultListView implements DefaultView {

	public static DefaultListView of(List<DefaultView> values) {
		return new DefaultListView(values, Optional.empty());
	}

	public static DefaultListView of(List<DefaultView> values, ConverterView converter) {
		return new DefaultListView(values, Optional.of(converter));
	}

	private final List<DefaultView> values;
	private final Optional<ConverterView> converter;

	private DefaultListView(List<DefaultView> values, Optional<ConverterView> converter) {
		this.values = values;
		this.converter = converter;
	}

	public List<DefaultView> values() {
		return values;
	}

	public Optional<ConverterView> converter() {
		return converter;
	}
}
