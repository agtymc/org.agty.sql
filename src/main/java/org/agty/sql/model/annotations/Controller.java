package org.agty.sql.model.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides controller behavior.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Controller {
    /**
     * Performs the value operation.
     * @return operation result
     */
    Class<?> value();
}
