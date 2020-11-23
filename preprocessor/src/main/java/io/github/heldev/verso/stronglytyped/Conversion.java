package io.github.heldev.verso.stronglytyped;

import io.github.heldev.verso.stronglytyped.type.ConcreteType;

import java.util.Objects;

public class Conversion {
	private final ConcreteType from;
	private final ConcreteType to;

	public Conversion(ConcreteType from, ConcreteType to) {
		this.from = from;
		this.to = to;
	}

	public ConcreteType getFrom() {
		return from;
	}

	public ConcreteType getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "Conversion{" +
				"from=" + from +
				", to=" + to +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Conversion that = (Conversion) o;
		return Objects.equals(from, that.from) && Objects.equals(to, that.to);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}
}
