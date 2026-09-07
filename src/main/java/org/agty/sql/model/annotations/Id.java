package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides id behavior.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Id {
    /**
     * Performs the force operation.
     * @return operation result
     */
    boolean force() default false;
}
