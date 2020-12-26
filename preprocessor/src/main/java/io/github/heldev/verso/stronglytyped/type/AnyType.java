package io.github.heldev.verso.stronglytyped.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AnyType {
	protected final String name; //tood maybe use Name

	public AnyType(String name) {
		this.name = name;
	}


	public static ConcreteType string() {
		return new ConcreteType("String");
	}

	public static ConcreteType boolean_() {
		return new ConcreteType("Boolean");
	}

	public static ConcreteType integer() {
		return new ConcreteType("Integer");
	}

	public static ConcreteType concreteList(ConcreteType parameter) {
		return new ConcreteType("List", parameter);
	}

	public static ConcreteType concreteSet(ConcreteType parameter) {
		return new ConcreteType("Set", parameter);
	}

	public static ConcreteType concreteMap(ConcreteType key, ConcreteType value) {
		return new ConcreteType("Map", key, value);
	}

	public static ConcreteType concrete(String name, ConcreteType... parameters) {
		return new ConcreteType(name, parameters);
	}

	public static TemplateType template(String name, List<AnyType> parameters) {
		return new TemplateType(name, parameters);
	}

	public static TemplateType template(String name, AnyType... parameters) {
		return new TemplateType(name, List.of(parameters));
	}

	public static ParameterType parameter(String name) {
		return new ParameterType(name);
	}

	public Optional<Map<ParameterType, ConcreteType>> matches(ConcreteType concreteType) {

		var typeArgumentsAccumulator = new HashMap<ParameterType, ConcreteType>();

		return matches(concreteType, typeArgumentsAccumulator)
				? Optional.of(Map.copyOf(typeArgumentsAccumulator))
				: Optional.empty();
	}

	protected abstract boolean matches(ConcreteType concreteType, HashMap<ParameterType, ConcreteType> typeArgumentsAccumulator);
}
