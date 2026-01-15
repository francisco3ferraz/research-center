package pt.ipleiria.dei.ei.estg.researchcenter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;

import java.security.Key;
import java.security.Principal;
@Provider
@Authenticated
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    @EJB
    private UserBean userBean;
    @Context
    private UriInfo uriInfo;
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new
                    NotAuthorizedException("Authorization header must be provided");
        }
        // Get token from the HTTP Authorization header
        String token = header.substring("Bearer".length()).trim();
        User user = userBean.findByUsername(getUsername(token));
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return user::getUsername;
            }
            @Override
            public boolean isUserInRole(String role) {
                if (user == null) return false;
                // Compare against UserRole name if available
                if (user.getRole() != null && user.getRole().name().equals(role)) return true;
                // Fallback to class-based role for backward compatibility
                return org.hibernate.Hibernate.getClass(user).getSimpleName().equals(role);
            }
            @Override
            public boolean isSecure() {
                return uriInfo.getAbsolutePath().toString().startsWith("https");
            }
            @Override
            public String getAuthenticationScheme() { return "Bearer"; }
        });
    }
    private String getUsername(String token) {
        Key key = Keys.hmacShaKeyFor(TokenIssuer.SECRET_KEY);
        try {
                return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new NotAuthorizedException("Invalid JWT");
        }
    }
}
