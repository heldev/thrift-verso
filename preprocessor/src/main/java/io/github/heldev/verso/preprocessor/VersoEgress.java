package io.github.heldev.verso.preprocessor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface VersoEgress {

    String thriftFile();
    String structName();
}
