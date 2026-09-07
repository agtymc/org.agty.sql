package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides table behavior.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {
    /**
     * Performs the name operation.
     * @return operation result
     */
    String name() default "";
    /**
     * Performs the schema operation.
     * @return operation result
     */
    String schema() default "";
}
