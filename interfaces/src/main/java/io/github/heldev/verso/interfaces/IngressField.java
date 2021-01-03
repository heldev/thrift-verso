package io.github.heldev.verso.interfaces;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Target(PARAMETER)
public @interface IngressField {

	int id();
}
