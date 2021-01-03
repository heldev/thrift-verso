package io.github.heldev.verso.interfaces;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
public @interface EgressField {

	int id();
}
