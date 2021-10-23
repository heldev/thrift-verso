package io.github.heldev.verso.preprocessors.reader;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.github.heldev.verso.preprocessors.reader.presentatition.ConverterView;
import io.github.heldev.verso.preprocessors.reader.presentatition.FieldView;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftBasic;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftBasicType;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftListType;
import io.github.heldev.verso.preprocessors.reader.presentatition.ThriftSetType;
import io.github.heldev.verso.preprocessors.reader.presentatition.View;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.heldev.verso.preprocessors.reader.presentatition.ThriftBasic.STRING;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReaderRendererTest {

	@Test
	public void should_render() {
		ReaderRenderer readerRenderer = new ReaderRenderer();

		FieldView authorField = new FieldView(
				1,
				"author",
				TypeName.get(String.class),
				true,
				false,
				new ThriftBasicType(STRING, Optional.empty()));

		FieldView ageField = new FieldView(
				2,
				"age",
				TypeName.get(Integer.class),
				false,
				false,
				new ThriftBasicType(ThriftBasic.I32, Optional.empty()));

		FieldView tagsField = new FieldView(
				3,
				"tags",
				ParameterizedTypeName.get(List.class, String.class),
				false,
				false,
				ThriftListType.of(
						new ThriftBasicType(STRING, Optional.empty())));

		FieldView expiry = new FieldView(
				4,
				"expiry",
				TypeName.get(ZonedDateTime.class),
				false,
				false,
				new ThriftBasicType(STRING, Optional.of(new ConverterView(TypeName.get(MyConverters.class), "stringToZonedDateTime", TypeName.get(ZonedDateTime.class)))));

		FieldView timelineSuperset = new FieldView(
				5,
				"timelineSuperset",
				ParameterizedTypeName.get(Superset.class, Timeline.class),
				false,
				false,
				ThriftListType.of(
						ThriftSetType.of(
								ThriftListType.of(
										new ThriftBasicType(STRING, Optional.of(new ConverterView(TypeName.get(MyConverters.class), "stringToZonedDateTime", TypeName.get(ZonedDateTime.class)))),
										new ConverterView(TypeName.get(MyConverters.class), "zonedDateTimeListToTimeline", TypeName.get(Timeline.class))
								)
						),
						new ConverterView(TypeName.get(MyConverters.class), "setListToSuperset", ParameterizedTypeName.get(Superset.class, Timeline.class))
				)
		);

		JavaFile file = readerRenderer.render(new View(
				TypeName.get(MyDto.class),
				"MyDtoReader",
				"io.github.heldev.verso.preprocessors.reader",
				asList(authorField, ageField, tagsField, expiry, timelineSuperset)));

		assertEquals(file.toString(), "");
	}
}


class MyDto {
	public MyDto(
			String name,
			Integer age,
			List<String> tags,
			ZonedDateTime expiry,
			Superset<Timeline> timelineSuperset) {

	}
}

abstract class MyConverters{

	public static ZonedDateTime stringToZonedDateTime(String source) {
		return ZonedDateTime.parse(source);
	}

	public static <T> Superset<T> setListToSuperset(List<Set<T>> source) {
		throw new RuntimeException("wip");
	}

	public static Timeline zonedDateTimeListToTimeline(List<ZonedDateTime> source) {
		throw new RuntimeException("wip");
	}
}

class Superset<T>{}
class Timeline{}
