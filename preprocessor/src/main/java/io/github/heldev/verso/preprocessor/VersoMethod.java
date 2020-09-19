package io.github.heldev.verso.preprocessor;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface VersoMethod {
    String value();
}
