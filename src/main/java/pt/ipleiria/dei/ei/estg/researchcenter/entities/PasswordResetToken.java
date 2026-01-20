package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@NamedQueries({
    @NamedQuery(
        name = "findValidToken",
        query = "SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now"
    ),
    @NamedQuery(
        name = "findTokenByUser",
        query = "SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId AND t.used = false AND t.expiresAt > :now"
    ),
    @NamedQuery(
        name = "deleteExpiredTokens",
        query = "DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now OR t.used = true"
    )
})
public class PasswordResetToken implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String token;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;
    
    @NotNull
    private LocalDateTime createdAt;
    
    @NotNull
    private LocalDateTime expiresAt;
    
    private boolean used = false;
    
    private LocalDateTime usedAt;
    
    // Default constructor
    public PasswordResetToken() {
    }
    
    // Constructor with parameters
    public PasswordResetToken(String token, User user, int expiryHours) {
        this.token = token;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(expiryHours);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}
