package io.github.heldev.verso.stronglytyped.type;

import java.util.List;
import java.util.Objects;

final public class TemplateType extends ParameterizedType<AnyType> {

	TemplateType(String name, AnyType... parameters) {
		super(name, List.of(parameters));
	}

	@Override
	public String toString() {
		return "TemplateType{" +
				"name='" + name + '\'' +
				", parameters=" + parameters +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (TemplateType) o;
		return Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters);
	}


	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
