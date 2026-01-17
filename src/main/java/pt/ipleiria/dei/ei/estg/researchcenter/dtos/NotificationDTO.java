package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDTO implements Serializable {
    
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Default constructor
    public NotificationDTO() {
    }

    // Constructor with parameters
    public NotificationDTO(Long id, Long userId, String type, String title, String message,
                          String relatedEntityType, Long relatedEntityId, boolean read,
                          LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    // Conversion methods
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getUser() != null ? notification.getUser().getId() : null,
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getRelatedEntityType(),
            notification.getRelatedEntityId(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }

    public static List<NotificationDTO> from(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationDTO::from)
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
