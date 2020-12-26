package io.github.heldev.verso.annotation;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.InvocationTargetException;

public class GlobalVersoWriter implements VersoWriter<Object> {

	public void write(Object object, TProtocol protocol) throws TException {
		try {
			var writerClassName = object.getClass().getCanonicalName() + "$$VersoWriter";
			Class<VersoWriter<? super Object>> writerClass = (Class) Class.forName(writerClassName);
			var writer = writerClass.getDeclaredConstructor().newInstance();

			writer.write(object, protocol);

		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Not a Thrift Verso class");
		}

	}
}
