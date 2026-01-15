package pt.ipleiria.dei.ei.estg.researchcenter.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require ownership of a resource.
 * The authenticated user must be the owner of the resource being accessed.
 *
 * Example: @RequireOwnership(parameterName = "username")
 * This will check if the authenticated user's username matches the path parameter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequireOwnership {
    /**
     * The name of the path parameter that contains the owner's identifier
     */
    String parameterName() default "username";

    /**
     * Roles that can bypass ownership check (e.g., administrators)
     */
    String[] bypassRoles() default {"Administrator"};
}

