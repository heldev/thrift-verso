package io.github.heldev.verso.stronglytyped.type;

import java.util.HashMap;
import java.util.List;


abstract class ParameterizedType<T extends AnyType> extends AnyType {
	protected final List<T> parameters;

	public ParameterizedType(String name, List<T> parameters) {
		super(name);
		this.parameters = parameters;
	}

	@Override
	public boolean matches(ConcreteType concreteType, HashMap<ParameterType, ConcreteType> typeArgumentsAccumulator) {

		return name.equals(concreteType.name)
				&& doParametersMatch(concreteType, typeArgumentsAccumulator);
	}

	private boolean doParametersMatch(ConcreteType concreteType, HashMap<ParameterType, ConcreteType> typeArgumentsAccumulator) {
		if (parameters.size() != concreteType.parameters.size()) {
			throw new IllegalArgumentException("Types should have the same number of parameters:\n %s\n%s".formatted(this, concreteType));
		} else {
			var parameterIterator = parameters.iterator();
			var concreteParameterIterator = concreteType.parameters.iterator();

			var isMatch = true;
			while (parameterIterator.hasNext() && isMatch) {
				var parameter = parameterIterator.next();
				var concreteParameter = concreteParameterIterator.next();

				isMatch = parameter.matches(concreteParameter, typeArgumentsAccumulator);
			}

			return isMatch;
		}
	}

}
