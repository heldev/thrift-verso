package io.github.heldev.verso.stronglytyped;


import io.github.heldev.verso.stronglytyped.type.ConcreteType;
import io.github.heldev.verso.stronglytyped.type.ParameterType;
import io.github.heldev.verso.stronglytyped.type.TemplateType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

//todo class and method naming
public class Converter {
	private final TemplateType from;
	private final TemplateType to;

	public Converter(TemplateType from, TemplateType to) {
		this.from = from;
		this.to = to;
	}

	public Optional<Set<Conversion>> findConversionConditions(Conversion conversion) {

		return from.matches(conversion.getFrom())
				.flatMap(fromArguments -> getConversions(conversion.getTo(), fromArguments));
	}

	private Optional<Set<Conversion>> getConversions(ConcreteType concreteTo, Map<ParameterType, ConcreteType> fromArguments) {

		return to.matches(concreteTo)
				.map(toArguments -> buildConversionsForConflictingParameters(fromArguments, toArguments));
	}

	private Set<Conversion> buildConversionsForConflictingParameters(
			Map<ParameterType, ConcreteType> fromMap,
			Map<ParameterType, ConcreteType> toMap) {

		return fromMap.entrySet()
				.stream()
				.map(entry -> new Conversion(entry.getValue(), toMap.getOrDefault(entry.getKey(), entry.getValue())))
				.filter(conversion -> ! conversion.getFrom().equals(conversion.getTo()))
				.collect(toSet());
	}

	@Override
	public String toString() {
		return "Converter{" +
				"from=" + from +
				", to=" + to +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Converter converter = (Converter) o;
		return Objects.equals(from, converter.from) && Objects.equals(to, converter.to);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}
}
