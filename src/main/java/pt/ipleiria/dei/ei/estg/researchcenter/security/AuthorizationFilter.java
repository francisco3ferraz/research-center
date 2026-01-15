package pt.ipleiria.dei.ei.estg.researchcenter.security;

import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
@Authenticated
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED)
            .entity("{\"error\": \"Access denied for this resource\"}").build();
    private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
            .entity("{\"error\": \"Access forbidden for this resource\"}").build();

    @Context
    private SecurityContext securityContext;

    @Context
    private ResourceInfo resourceInfo;

    @EJB
    private UserBean userBean;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();

        // Check DenyAll first
        if (isDenied(method, resourceClass)) {
            requestContext.abortWith(ACCESS_DENIED);
            return;
        }

        // Check PermitAll
        if (isPermitted(method, resourceClass)) {
            return;
        }

        // Check custom annotations
        if (!checkCustomAuthorizations(method, resourceClass, requestContext)) {
            return; // Already aborted in check method
        }

        // Check standard RolesAllowed
        if (!checkRolesAllowed(method, resourceClass)) {
            requestContext.abortWith(ACCESS_FORBIDDEN);
        }
    }

    private boolean isDenied(Method method, Class<?> resourceClass) {
        // Method-level DenyAll takes precedence
        if (method.isAnnotationPresent(DenyAll.class)) {
            return true;
        }
        // Class-level DenyAll unless method has PermitAll or RolesAllowed
        if (resourceClass.isAnnotationPresent(DenyAll.class)) {
            return !method.isAnnotationPresent(PermitAll.class)
                   && !method.isAnnotationPresent(RolesAllowed.class);
        }
        return false;
    }

    private boolean isPermitted(Method method, Class<?> resourceClass) {
        // Method-level PermitAll takes precedence
        if (method.isAnnotationPresent(PermitAll.class)) {
            return true;
        }
        // Class-level PermitAll unless method has DenyAll or RolesAllowed
        if (resourceClass.isAnnotationPresent(PermitAll.class)) {
            return !method.isAnnotationPresent(DenyAll.class)
                   && !method.isAnnotationPresent(RolesAllowed.class);
        }
        return false;
    }

    private boolean checkCustomAuthorizations(Method method, Class<?> resourceClass,
                                              ContainerRequestContext requestContext) {
        // Check RequireOwnership annotation
        if (method.isAnnotationPresent(RequireOwnership.class)) {
            if (!checkOwnership(method, requestContext)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"You can only access your own resources\"}").build());
                return false;
            }
        }

        // Check RequirePermission annotation
        if (method.isAnnotationPresent(RequirePermission.class) ||
            resourceClass.isAnnotationPresent(RequirePermission.class)) {
            if (!checkPermissions(method, resourceClass)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Insufficient permissions\"}").build());
                return false;
            }
        }

        // Check RequireAction annotation
        if (method.isAnnotationPresent(RequireAction.class) ||
            resourceClass.isAnnotationPresent(RequireAction.class)) {
            if (!checkAction(method, resourceClass)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Action not authorized\"}").build());
                return false;
            }
        }

        // Check ConditionalAuth annotation
        if (method.isAnnotationPresent(ConditionalAuth.class)) {
            ConditionalAuth conditionalAuth = method.getAnnotation(ConditionalAuth.class);
            if (!checkConditionalAuth(conditionalAuth, requestContext)) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"" + conditionalAuth.message() + "\"}").build());
                return false;
            }
        }

        return true;
    }

    private boolean checkOwnership(Method method, ContainerRequestContext requestContext) {
        RequireOwnership annotation = method.getAnnotation(RequireOwnership.class);
        String username = securityContext.getUserPrincipal().getName();

        // Check if user has bypass role (e.g., Administrator)
        for (String bypassRole : annotation.bypassRoles()) {
            if (securityContext.isUserInRole(bypassRole)) {
                return true;
            }
        }

        // Get the parameter from path
        String paramValue = requestContext.getUriInfo()
                .getPathParameters()
                .getFirst(annotation.parameterName());

        // Check if authenticated user matches the parameter
        return username != null && username.equals(paramValue);
    }

    private boolean checkPermissions(Method method, Class<?> resourceClass) {
        RequirePermission annotation = method.isAnnotationPresent(RequirePermission.class)
                ? method.getAnnotation(RequirePermission.class)
                : resourceClass.getAnnotation(RequirePermission.class);

        if (annotation == null) {
            return true;
        }

        String username = securityContext.getUserPrincipal().getName();
        String[] requiredPermissions = annotation.value();

        if (annotation.logic() == RequirePermission.Logic.AND) {
            // User must have ALL permissions
            return Arrays.stream(requiredPermissions)
                    .allMatch(perm -> userBean.userHasPermission(username, perm));
        } else {
            // User must have AT LEAST ONE permission (OR logic)
            return Arrays.stream(requiredPermissions)
                    .anyMatch(perm -> userBean.userHasPermission(username, perm));
        }
    }

    private boolean checkAction(Method method, Class<?> resourceClass) {
        RequireAction annotation = method.isAnnotationPresent(RequireAction.class)
                ? method.getAnnotation(RequireAction.class)
                : resourceClass.getAnnotation(RequireAction.class);

        if (annotation == null) {
            return true;
        }

        // Check if user has any of the allowed roles
        if (annotation.allowedRoles().length > 0) {
            boolean hasRole = Arrays.stream(annotation.allowedRoles())
                    .anyMatch(securityContext::isUserInRole);
            if (!hasRole) {
                return false;
            }
        }

        // Build permission string from resource and action
        String permissionString = annotation.resource().toLowerCase() +
                                  annotation.action().name().toLowerCase();

        String username = securityContext.getUserPrincipal().getName();
        return userBean.userHasPermission(username, permissionString);
    }

    private boolean checkConditionalAuth(ConditionalAuth annotation,
                                        ContainerRequestContext requestContext) {
        // This is a placeholder for custom conditional logic
        // In a real implementation, you would have a strategy pattern or
        // lookup mechanism to execute different conditions
        String condition = annotation.condition();

        // Example conditions (extend as needed)
        switch (condition) {
            case "isEnrolledInCourse":
                return checkEnrollmentCondition(requestContext);
            case "isTeacherOfCourse":
                return checkTeacherOfCourseCondition(requestContext);
            default:
                return true; // Unknown conditions pass by default
        }
    }

    private boolean checkEnrollmentCondition(ContainerRequestContext requestContext) {
        // Example: Check if student is enrolled in the course
        // This would need to query the database
        String username = securityContext.getUserPrincipal().getName();
        // Implementation would check enrollment status
        return true; // Placeholder
    }

    private boolean checkTeacherOfCourseCondition(ContainerRequestContext requestContext) {
        // Example: Check if teacher teaches the course
        String username = securityContext.getUserPrincipal().getName();
        // Implementation would check teaching assignment
        return true; // Placeholder
    }

    private boolean checkRolesAllowed(Method method, Class<?> resourceClass) {
        Set<String> allowedRoles = new HashSet<>();

        // Collect roles from class-level annotation
        if (resourceClass.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = resourceClass.getAnnotation(RolesAllowed.class);
            allowedRoles.addAll(Arrays.asList(rolesAnnotation.value()));
        }

        // Collect roles from method-level annotation (overrides class-level)
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            // Method-level replaces class-level
            allowedRoles.clear();
            allowedRoles.addAll(Arrays.asList(rolesAnnotation.value()));
        }

        // If no roles specified, allow access
        if (allowedRoles.isEmpty()) {
            return true;
        }

        // Check if user has any of the allowed roles
        return allowedRoles.stream().anyMatch(securityContext::isUserInRole);
    }
}
