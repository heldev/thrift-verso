package io.github.heldev.verso.annotation;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface VersoIngress {

	String thriftFile();
	String structName();
}
