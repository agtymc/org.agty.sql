package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides entity behavior.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {}
