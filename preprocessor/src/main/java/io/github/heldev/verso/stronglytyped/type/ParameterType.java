package io.github.heldev.verso.stronglytyped.type;

import java.util.HashMap;
import java.util.Objects;

public class ParameterType extends AnyType {

	ParameterType(String name) {
		super(name);
	}

	@Override
	public boolean matches(ConcreteType concreteType, HashMap<ParameterType, ConcreteType> typeArgumentsAccumulator) {
		typeArgumentsAccumulator.putIfAbsent(this, concreteType);

		return typeArgumentsAccumulator.get(this).equals(concreteType);
	}

	@Override
	public String toString() {
		return "ParameterType{" +
				"name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (ParameterType) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
