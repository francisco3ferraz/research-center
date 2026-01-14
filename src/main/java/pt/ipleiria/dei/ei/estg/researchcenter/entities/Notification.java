package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@NamedQueries({
    @NamedQuery(
        name = "getUserNotifications",
        query = "SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC"
    ),
    @NamedQuery(
        name = "getUnreadNotifications",
        query = "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.createdAt DESC"
    ),
    @NamedQuery(
        name = "countUnreadNotifications",
        query = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false"
    )
})
public class Notification implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private Collaborator user;
    
    @NotBlank
    private String type; // NEW_PUBLICATION_WITH_TAG, NEW_COMMENT_ON_TAG, etc.
    
    @NotBlank
    private String title;
    
    @Column(length = 1000)
    @NotBlank
    private String message;
    
    @NotBlank
    private String relatedEntityType; // PUBLICATION, COMMENT, etc.
    
    private Long relatedEntityId;
    
    private boolean read = false;
    
    @NotNull
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    // Default constructor
    public Notification() {
    }
    
    // Constructor with parameters
    public Notification(Collaborator user, String type, String title, String message,
                       String relatedEntityType, Long relatedEntityId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Collaborator getUser() {
        return user;
    }
    
    public void setUser(Collaborator user) {
        this.user = user;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getRelatedEntityType() {
        return relatedEntityType;
    }
    
    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }
    
    public Long getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
