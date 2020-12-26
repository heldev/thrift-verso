package io.github.heldev.verso.annotation;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

@Target(PARAMETER)
public @interface IngressField {

	int id();
}
