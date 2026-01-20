package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.PasswordResetToken;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Logger;

@Stateless
public class PasswordResetBean {
    
    private static final Logger logger = Logger.getLogger(PasswordResetBean.class.getName());
    private static final int TOKEN_EXPIRY_HOURS = 1;
    private static final String FRONTEND_URL = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000");
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private EmailBean emailBean;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Request password reset - generates token and sends email
     */
    public boolean requestPasswordReset(String email) {
        // Find user by email
        User user = userBean.findByEmail(email);
        if (user == null) {
            // Return true even if user not found (security best practice - don't leak info)
            logger.info("Password reset requested for non-existent email: " + email);
            return true;
        }
        
        // Invalidate any existing tokens for this user
        invalidateExistingTokens(user.getId());
        
        // Generate new token
        String tokenValue = generateSecureToken();
        PasswordResetToken token = new PasswordResetToken(tokenValue, user, TOKEN_EXPIRY_HOURS);
        em.persist(token);
        
        // Build reset URL
        String resetUrl = FRONTEND_URL + "/reset-password?token=" + tokenValue;
        
        // Send email
        try {
            emailBean.sendPasswordResetEmail(
                user.getEmail(),
                user.getName(),
                tokenValue,
                resetUrl
            );
            logger.info("Password reset email sent to: " + email);
        } catch (MessagingException e) {
            logger.severe("Failed to send password reset email: " + e.getMessage());
            // Still return true - token was created, user can try again
        }
        
        return true;
    }
    
    /**
     * Reset password using token
     */
    public boolean resetPassword(String tokenValue, String newPassword) throws MyEntityNotFoundException {
        // Find valid token
        var tokens = em.createNamedQuery("findValidToken", PasswordResetToken.class)
                .setParameter("token", tokenValue)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        
        if (tokens.isEmpty()) {
            throw new MyEntityNotFoundException("Token inválido ou expirado");
        }
        
        PasswordResetToken token = tokens.get(0);
        User user = token.getUser();
        
        // Update password
        userBean.setPassword(user.getId(), newPassword);
        
        // Mark token as used
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        
        logger.info("Password reset successful for user: " + user.getUsername());
        
        return true;
    }
    
    /**
     * Validate token without using it
     */
    public boolean isTokenValid(String tokenValue) {
        var tokens = em.createNamedQuery("findValidToken", PasswordResetToken.class)
                .setParameter("token", tokenValue)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        
        return !tokens.isEmpty();
    }
    
    /**
     * Invalidate existing tokens for a user
     */
    private void invalidateExistingTokens(Long userId) {
        var tokens = em.createNamedQuery("findTokenByUser", PasswordResetToken.class)
                .setParameter("userId", userId)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        
        for (PasswordResetToken token : tokens) {
            token.setUsed(true);
            token.setUsedAt(LocalDateTime.now());
        }
    }
    
    /**
     * Clean up expired tokens (can be called by a scheduler)
     */
    public int cleanupExpiredTokens() {
        return em.createNamedQuery("deleteExpiredTokens")
                .setParameter("now", LocalDateTime.now())
                .executeUpdate();
    }
    
    /**
     * Generate a secure random token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
