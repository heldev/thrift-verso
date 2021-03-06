package io.github.heldev.verso.interfaces;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface VersoIngress {

	String thriftFile();
	String structName();
}
