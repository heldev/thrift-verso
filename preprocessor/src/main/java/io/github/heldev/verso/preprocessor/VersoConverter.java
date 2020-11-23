package io.github.heldev.verso.preprocessor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
public @interface VersoConverter {
}
