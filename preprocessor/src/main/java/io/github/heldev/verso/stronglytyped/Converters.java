package io.github.heldev.verso.stronglytyped;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Converters {
	private final Set<Converter> allConverters;

	public Converters(Set<Converter> converters) {
		this.allConverters = new HashSet<>(converters);
	}

	public Optional<List<Converter>> canDo(Conversion conversion) {

		return allConverters.stream()
				.map(converter -> canDo(conversion, converter))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private Optional<List<Converter>> canDo(Conversion conversion, Converter converter) {
		System.out.printf("Trying %s\n on %s\n", converter, conversion);
		Optional<Set<Conversion>> conversionConditions = converter.findConversionConditions(conversion);

		return conversionConditions
				.flatMap(this::canDoAll)
				.map(converters -> prepend(converter, converters));
	}

	private Optional<List<Converter>> canDoAll(Set<Conversion> subConversions) {

		Set<Optional<List<Converter>>> subConverters = subConversions.stream()
				.map(this::canDo)
				.collect(toSet());

		return concatIfAllPresent(subConverters);
	}

	private <T> Optional<List<T>> concatIfAllPresent(Set<Optional<List<T>>> setOfOptionalLists) {

		return setOfOptionalLists.stream().allMatch(Optional::isPresent)
				? Optional.of(concat(setOfOptionalLists))
				: Optional.empty();
	}

	private <T> List<T> concat(Set<Optional<List<T>>> setOfOptionalLists) {

		return setOfOptionalLists.stream()
				.filter(Optional::isPresent)
				.map(Optional::get)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private <T> List<T> prepend(T item, List<T> list) {

		return Stream.concat(Stream.of(item), list.stream())
				.collect(toList());
	}
}
