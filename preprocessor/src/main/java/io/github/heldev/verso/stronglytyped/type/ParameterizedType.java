package io.github.heldev.verso.stronglytyped.type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;


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
			throw new IllegalArgumentException(format("Types should have the same number of parameters:\n %s\n%s", this, concreteType));
		} else {
			Iterator<T> parameterIterator = parameters.iterator();
			Iterator<ConcreteType> concreteParameterIterator = concreteType.parameters.iterator();

			boolean isMatch = true;
			while (parameterIterator.hasNext() && isMatch) {
				T parameter = parameterIterator.next();
				ConcreteType concreteParameter = concreteParameterIterator.next();

				isMatch = parameter.matches(concreteParameter, typeArgumentsAccumulator);
			}

			return isMatch;
		}
	}

}
