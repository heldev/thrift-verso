package io.github.heldev.verso.preprocessors.reader.presentatition;

import com.squareup.javapoet.TypeName;

import java.util.Optional;

public interface ThriftTypeView {
	TypeName targetType();
	Optional<ConverterView> converter();

}

