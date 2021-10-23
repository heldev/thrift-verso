package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public
class ThriftSetType implements ThriftTypeView {

	public static ThriftSetType of(ThriftTypeView subtype) {
		return new ThriftSetType(subtype, Optional.empty());
	}

	public static ThriftSetType of(ThriftTypeView subtype, ConverterView converter) {
		return new ThriftSetType(subtype, Optional.of(converter));
	}

	private final ThriftTypeView subtype;
	private final Optional<ConverterView> converter;

	private ThriftSetType(
			ThriftTypeView subtype,
			Optional<ConverterView> converter) {
		this.subtype = subtype;
		this.converter = converter;
	}

	@Override
	public TypeName targetType() {

		return converter
				.map(ConverterView::targetType)
				.orElse(ParameterizedTypeName.get(ClassName.get(Set.class), subtype.targetType()));
	}

	public ThriftTypeView subtype() {
		return subtype;
	}

	@Override
	public Optional<ConverterView> converter() {
		return converter;
	}

}
