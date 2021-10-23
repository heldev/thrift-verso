package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.TypeName;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ThriftBasicType implements ThriftTypeView {

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
		String name = thriftBasic.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}
