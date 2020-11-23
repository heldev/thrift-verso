package io.github.heldev.verso.preprocessor;

public interface Converter<S, T> {
	T convert(S source);
}
