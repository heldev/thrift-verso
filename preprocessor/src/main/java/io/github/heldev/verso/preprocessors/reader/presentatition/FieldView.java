package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.TypeName;

//todo defaults
public class FieldView {
	private final Integer id;
	private final String name;
	private final TypeName builderFieldType;
	private final Boolean throwIfNotProvided;
	private final Boolean wrapInOptional;
	private final ThriftTypeView thriftType;

	public FieldView(
			Integer id,
			String name,
			TypeName builderFieldType,
			Boolean throwIfNotProvided,
			Boolean wrapInOptional,
			ThriftTypeView thriftType) {

		this.id = id;
		this.name = name;
		this.builderFieldType = builderFieldType;
		this.throwIfNotProvided = throwIfNotProvided;
		this.wrapInOptional = wrapInOptional;
		this.thriftType = thriftType;
	}

	public Integer id() {
		return id;
	}

	public String name() {
		return name;
	}

	public TypeName builderFieldType() {
		return builderFieldType;
	}

	public Boolean throwIfNotProvided() {
		return throwIfNotProvided;
	}

	public Boolean wrapInOptional() {
		return wrapInOptional;
	}

	public ThriftTypeView thriftType() {
		return thriftType;
	}

}
