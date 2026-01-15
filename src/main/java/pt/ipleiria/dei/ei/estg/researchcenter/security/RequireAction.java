package pt.ipleiria.dei.ei.estg.researchcenter.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific actions to be authorized.
 * Actions represent specific operations that can be performed on resources.
 *
 * Example: @RequireAction(resource = "Course", action = "DELETE")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequireAction {
    /**
     * The resource type being accessed (e.g., "Course", "Student", "Grade")
     */
    String resource();

    /**
     * The action being performed (e.g., "CREATE", "READ", "UPDATE", "DELETE")
     */
    Action action();

    /**
     * Roles that are allowed to perform this action
     */
    String[] allowedRoles() default {};

    enum Action {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        MANAGE,      // Full CRUD access
        ENROLL,      // Student-specific
        GRADE,       // Grade management
        COORDINATE,  // Course coordination
        APPROVE      // Approval actions
    }
}

