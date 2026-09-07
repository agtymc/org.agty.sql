package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides schema name behavior.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface SchemaName {
    /**
     * Performs the value operation.
     * @return operation result
     */
    String value() default "";
}
