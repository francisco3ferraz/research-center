package pt.ipleiria.dei.ei.estg.researchcenter.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to conditionally authorize based on custom logic.
 * Can be used for complex authorization scenarios.
 *
 * Example: @ConditionalAuth(condition = "isEnrolledInCourse")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConditionalAuth {
    /**
     * The name of the condition to check
     */
    String condition();

    /**
     * Error message to return if authorization fails
     */
    String message() default "Access denied";
}
