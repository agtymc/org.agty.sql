package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides table name behavior.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface TableName {
    /**
     * Performs the value operation.
     * @return operation result
     */
    String value() default "";
}
