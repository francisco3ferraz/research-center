package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import java.util.Map;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.AuthDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PasswordDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.UserDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;
import pt.ipleiria.dei.ei.estg.researchcenter.security.TokenIssuer;

@Path("auth")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AuthService {
    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    public Response authenticate(@Valid AuthDTO auth) {
        if (userBean.canLogin(auth.getUsername(), auth.getPassword())) {
            userBean.updateLastLogin(auth.getUsername());
            String token = TokenIssuer.issue(auth.getUsername());
            return Response.ok(Map.of("token", token)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("/user")
    @Authenticated
    public Response getCurrentUser() {
        String username = securityContext.getUserPrincipal().getName();
        User user = userBean.findByUsername(username);
        return Response.ok(UserDTO.from(user)).build();
    }

    @POST
    @Path("/set-password")
    @Authenticated
    public Response changePasswordPost(@Valid PasswordDTO passwordDTO) {
        return changePasswordInternal(passwordDTO);
    }

    @PATCH
    @Path("/set-password")
    @Authenticated
    public Response changePasswordPatch(@Valid PasswordDTO passwordDTO) {
        return changePasswordInternal(passwordDTO);
    }

    private Response changePasswordInternal(PasswordDTO passwordDTO) {
        // Get the authenticated user's username from the security context
        String username = securityContext.getUserPrincipal().getName();

        // Validate that new password matches confirmation
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("New password and confirmation do not match")
                    .build();
        }

        // Attempt to change the password
        boolean success = userBean.changePassword(
                username,
                passwordDTO.getOldPassword(),
                passwordDTO.getNewPassword()
        );

        if (success) {
            return Response.ok("Password changed successfully").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Old password is incorrect")
                    .build();
        }
    }
    
    @EJB
    private pt.ipleiria.dei.ei.estg.researchcenter.ejbs.PasswordResetBean passwordResetBean;
    
    /**
     * Request password reset - sends email with reset token
     * POST /api/auth/request-reset
     */
    @POST
    @Path("/request-reset")
    public Response requestPasswordReset(Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email is required"))
                    .build();
        }
        
        // Always return success to prevent email enumeration attacks
        passwordResetBean.requestPasswordReset(email);
        
        return Response.ok(Map.of(
            "message", "Se o email existir no sistema, receberá instruções para redefinir a password."
        )).build();
    }
    
    /**
     * Reset password with token
     * POST /api/auth/reset-password
     */
    @POST
    @Path("/reset-password")
    public Response resetPassword(Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");
        
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Token is required"))
                    .build();
        }
        
        if (newPassword == null || newPassword.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "New password is required"))
                    .build();
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Passwords do not match"))
                    .build();
        }
        
        try {
            passwordResetBean.resetPassword(token, newPassword);
            return Response.ok(Map.of("message", "Password redefinida com sucesso")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Validate reset token
     * GET /api/auth/validate-token?token=xxx
     */
    @GET
    @Path("/validate-token")
    public Response validateToken(@QueryParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("valid", false, "message", "Token is required"))
                    .build();
        }
        
        boolean valid = passwordResetBean.isTokenValid(token);
        return Response.ok(Map.of("valid", valid)).build();
    }
}
