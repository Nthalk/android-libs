package com.iodesystems.android.jsi.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsInterfaceMethod {
    Mode value();

    String name() default "";

    enum Mode {
        UI,
        BACKGROUND
    }
}
