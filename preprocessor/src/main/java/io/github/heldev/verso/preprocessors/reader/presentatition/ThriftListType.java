package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public
class ThriftListType implements ThriftTypeView {

	public static ThriftListType of(ThriftTypeView subtype) {
		return new ThriftListType(subtype, Optional.empty());
	}

	public static ThriftListType of(ThriftTypeView subtype, ConverterView converter) {
		return new ThriftListType(subtype, Optional.of(converter));
	}

	private final ThriftTypeView subtype;
	private final Optional<ConverterView> converter;

	private ThriftListType(
			ThriftTypeView subtype,
			Optional<ConverterView> converter) {
		this.subtype = subtype;
		this.converter = converter;
	}

	@Override
	public TypeName targetType() {

		return converter
				.map(ConverterView::targetType)
				.orElse(ParameterizedTypeName.get(ClassName.get(List.class), subtype.targetType()));
	}

	public ThriftTypeView subtype() {
		return subtype;
	}

	@Override
	public Optional<ConverterView> converter() {
		return converter;
	}

}
