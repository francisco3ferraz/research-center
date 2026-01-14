package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@NamedQueries({
    @NamedQuery(
        name = "getUserActivityLog",
        query = "SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC"
    ),
    @NamedQuery(
        name = "getPublicationHistory",
        query = "SELECT a FROM ActivityLog a WHERE a.entityType = 'PUBLICATION' AND a.entityId = :publicationId ORDER BY a.timestamp DESC"
    )
})
public class ActivityLog implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;
    
    @NotBlank
    private String action; // UPLOAD_PUBLICATION, ADD_COMMENT, ADD_RATING, CREATE, UPDATE, etc.
    
    @NotBlank
    private String entityType; // PUBLICATION, COMMENT, RATING, TAG, etc.
    
    private Long entityId;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 1000)
    private String changedFields; // Comma-separated list for UPDATE actions
    
    @NotNull
    private LocalDateTime timestamp;
    
    // Default constructor
    public ActivityLog() {
    }
    
    // Constructor with parameters
    public ActivityLog(User user, String action, String entityType, Long entityId, String description) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getChangedFields() {
        return changedFields;
    }
    
    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}