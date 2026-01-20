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
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class PasswordResetBean {
    
    private static final Logger logger = Logger.getLogger("ejbs.PasswordResetBean");
    private static final int TOKEN_EXPIRY_HOURS = 1;
    private static final String FRONTEND_URL = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000");
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private UserBean userBean;
    
    @EJB
    private EmailBean emailBean;
    
    public PasswordResetToken find(Long id) {
        return em.find(PasswordResetToken.class, id);
    }
    
    public PasswordResetToken findValidToken(String tokenValue) {
        List<PasswordResetToken> tokens = em.createNamedQuery("findValidToken", PasswordResetToken.class)
                .setParameter("token", tokenValue)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        return tokens.isEmpty() ? null : tokens.get(0);
    }
    
    public boolean requestPasswordReset(String email) {
        User user = userBean.findByEmail(email);
        if (user == null) {
            logger.info("Password reset requested for non-existent email: " + email);
            return true;
        }
        
        // Invalidar tokens anteriores
        invalidateExistingTokens(user.getId());
        
        // Gerar novo token
        String tokenValue = generateToken();
        PasswordResetToken token = new PasswordResetToken(tokenValue, user, TOKEN_EXPIRY_HOURS);
        em.persist(token);
        
        // Enviar email
        String resetUrl = FRONTEND_URL + "/auth/reset-password?token=" + tokenValue;
        try {
            emailBean.sendPasswordResetEmail(user.getEmail(), user.getName(), tokenValue, resetUrl);
            logger.info("Password reset email sent to: " + email);
        } catch (MessagingException e) {
            logger.warning("Failed to send password reset email: " + e.getMessage());
        }
        
        return true;
    }
    
    public boolean resetPassword(String tokenValue, String newPassword) throws MyEntityNotFoundException {
        PasswordResetToken token = findValidToken(tokenValue);
        if (token == null) {
            throw new MyEntityNotFoundException("Token inválido ou expirado");
        }
        
        User user = token.getUser();
        userBean.setPassword(user.getId(), newPassword);
        
        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        
        logger.info("Password reset successful for user: " + user.getUsername());
        return true;
    }
    
    public boolean isTokenValid(String tokenValue) {
        return findValidToken(tokenValue) != null;
    }
    
    private void invalidateExistingTokens(Long userId) {
        List<PasswordResetToken> tokens = em.createNamedQuery("findTokenByUser", PasswordResetToken.class)
                .setParameter("userId", userId)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        for (PasswordResetToken t : tokens) {
            t.setUsed(true);
            t.setUsedAt(LocalDateTime.now());
        }
    }
    
    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
