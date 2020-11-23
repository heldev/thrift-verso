package io.github.heldev.verso.app.man;

import io.github.heldev.verso.preprocessor.VersoConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class VersoConverters {

	@VersoConverter
	public static <T> ArrayList<T> listToArrayList(List<T> source) {
		return new ArrayList<>(source);
	}

	@VersoConverter
	public static <E> ArrayList<E> e(List<E> source) {
		return new ArrayList<>(source);
	}

	@VersoConverter
	public static <T> ArrayList<T> t(List<T> source) {
		return new ArrayList<>(source);
	}

	@VersoConverter
	public static <T> LinkedList<T> listToLinkedList(List<T> source) {
		return new LinkedList<>(source);
	}

	@VersoConverter
	public static <T> Vector<T> listToVector(List<T> source) {
		return new Vector<>(source);
	}

	@VersoConverter
	public static Date stringToDate(String source) {
		return new Date(source);
	}

	@VersoConverter
	public static TimeSeries integerListToTimeSeries(List<Integer> source) {
		return new TimeSeries(source);
	}

}
