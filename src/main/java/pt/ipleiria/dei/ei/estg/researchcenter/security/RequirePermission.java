package pt.ipleiria.dei.ei.estg.researchcenter.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific permissions for accessing a resource.
 * Permissions are more granular than roles and can represent specific actions.
 *
 * Example: @RequirePermission({"course:create", "course:edit"})
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequirePermission {
    String[] value();

    /**
     * Logical operator to combine multiple permissions
     * AND = user must have ALL permissions
     * OR = user must have AT LEAST ONE permission
     */
    Logic logic() default Logic.OR;

    enum Logic {
        AND, OR
    }
}

