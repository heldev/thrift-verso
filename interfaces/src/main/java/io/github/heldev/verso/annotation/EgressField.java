package io.github.heldev.verso.annotation;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
public @interface EgressField {

	int id();
}
