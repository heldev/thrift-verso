package io.github.heldev.verso.stronglytyped.type;

import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

final public class ConcreteType extends ParameterizedType<ConcreteType> {

	ConcreteType(String name, ConcreteType... parameters) {
		super(name, asList(parameters));
	}

	@Override
	public String toString() {

		return "ConcreteType{" +
				"name='" + name + '\'' +
				", parameters=" + parameters +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConcreteType that = (ConcreteType) o;
		return Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parameters);
	}
}
