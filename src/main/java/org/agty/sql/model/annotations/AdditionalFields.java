package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Marks a model method that supplies additional mapped fields. */
@Target(METHOD)
@Retention(RUNTIME)
public @interface AdditionalFields {}
