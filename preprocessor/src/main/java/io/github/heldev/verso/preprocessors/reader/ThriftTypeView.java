package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ThriftTypeView {
	TypeName targetType();
	Optional<ConverterView> converter();
}

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class ThriftBasicType implements ThriftTypeView {

	private final ThriftBasic thriftBasic;
	private final Optional<ConverterView> converter;

	public ThriftBasicType(
			ThriftBasic thriftBasic,
			Optional<ConverterView> converter) {
		this.thriftBasic = thriftBasic;
		this.converter = converter;
	}

	@Override
	public TypeName targetType() {

		return converter
				.map(ConverterView::targetType)
				.orElse(getTypeName());
	}

	private TypeName getTypeName() {
		switch (thriftBasic) {
			case I32:
				return TypeName.get(Integer.class);

			case STRING:
				return TypeName.get(String.class);

			default:
				throw new RuntimeException("whoopsie");
		}
	}

	@Override
	public Optional<ConverterView> converter() {
		return converter;
	}

	public String methodRoot() {
		var name = thriftBasic.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}

enum ThriftBasic {

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

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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
