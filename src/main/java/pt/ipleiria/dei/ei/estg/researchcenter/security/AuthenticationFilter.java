package pt.ipleiria.dei.ei.estg.researchcenter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;

import java.security.Key;
import java.security.Principal;
import java.util.Map;

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
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        
        // Get token from the HTTP Authorization header
        String token = header.substring("Bearer".length()).trim();
        String username;
        try {
            username = getUsername(token);
        } catch (Exception e) {
             System.out.println("[AuthFilter] Invalid JWT: " + e.getMessage());
             throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid JWT", "details", e.getMessage()))
                    .build());
        }
        
        User user = userBean.findByUsername(username);
        
        if (user == null) {
            System.out.println("[AuthFilter] User not found in DB: " + username);
            // Return detailed error to help debugging
            throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "User not found", "username", String.valueOf(username)))
                    .build());
        }
        
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return user::getUsername;
            }
            @Override
            public boolean isUserInRole(String role) {
                if (user.getRole() != null && user.getRole().name().equals(role)) return true;
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
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
