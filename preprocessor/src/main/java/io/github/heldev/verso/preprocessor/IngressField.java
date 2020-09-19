package io.github.heldev.verso.preprocessor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Target(PARAMETER)
public @interface IngressField {

    int id();
}
