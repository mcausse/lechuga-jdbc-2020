package org.lechuga.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lechuga.handler.ColumnHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Handler {

	Class<? extends ColumnHandler> value();

	String[] args() default {};
}
