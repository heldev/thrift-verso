package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.TypeName;

import java.util.List;

public class View {
	final TypeName dtoType;
	final String readerName; //todo type safety
	final String readerPackage;
	final List<FieldView> fields;

	public View(TypeName dtoType, String readerName, String readerPackage, List<FieldView> fields) {
		this.dtoType = dtoType;
		this.readerName = readerName;
		this.readerPackage = readerPackage;
		this.fields = fields;
	}
}
