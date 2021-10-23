package io.github.heldev.verso;

import io.github.heldev.verso.interfaces.VersoWriter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.InvocationTargetException;

public class GlobalVersoWriter implements VersoWriter<Object> {

	public void write(Object object, TProtocol protocol) throws TException {
		try {
			String writerClassName = object.getClass().getCanonicalName() + "$$VersoWriter";
			Class<VersoWriter<? super Object>> writerClass = (Class) Class.forName(writerClassName);
			VersoWriter<? super Object> writer = writerClass.getDeclaredConstructor().newInstance();

			writer.write(object, protocol);

		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Not a Thrift Verso class");
		}

	}
}
