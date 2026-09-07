package org.agty.sql.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides column behavior.
 */
@Target(value = ElementType.FIELD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * Performs the name operation.
     * @return operation result
     */
    String name() default "";
    /**
     * Performs the skip operation.
     * @return operation result
     */
    boolean skip() default false;
    /**
     * Performs the skip if null operation.
     * @return operation result
     */
    boolean skipIfNull() default false;
}
