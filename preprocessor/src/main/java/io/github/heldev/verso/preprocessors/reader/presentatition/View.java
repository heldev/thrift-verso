package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.TypeName;

import java.util.List;

public class View {
	private final TypeName dtoType;
	private final String readerName; //todo type safety
	private final String readerPackage;
	private final List<FieldView> fields;

	public View(TypeName dtoType, String readerName, String readerPackage, List<FieldView> fields) {
		this.dtoType = dtoType;
		this.readerName = readerName;
		this.readerPackage = readerPackage;
		this.fields = fields;
	}

	public TypeName dtoType() {
		return dtoType;
	}

	public String readerName() {
		return readerName;
	}

	public String readerPackage() {
		return readerPackage;
	}

	public List<FieldView> fields() {
		return fields;
	}
}
