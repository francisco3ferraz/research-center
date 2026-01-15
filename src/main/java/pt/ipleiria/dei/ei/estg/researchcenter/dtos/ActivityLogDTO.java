package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.ActivityLog;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityLogDTO implements Serializable {

    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private String changedFields;
    private LocalDateTime timestamp;

    public ActivityLogDTO() {
    }

    public ActivityLogDTO(Long id, Long userId, String action, String entityType,
                         Long entityId, String description, String changedFields,
                         LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.changedFields = changedFields;
        this.timestamp = timestamp;
    }

    public static ActivityLogDTO from(ActivityLog log) {
        if (log == null) return null;
        return new ActivityLogDTO(
            log.getId(),
            log.getUser() != null ? log.getUser().getId() : null,
            log.getAction(),
            log.getEntityType(),
            log.getEntityId(),
            log.getDescription(),
            log.getChangedFields(),
            log.getTimestamp()
        );
    }

    public static List<ActivityLogDTO> from(List<ActivityLog> logs) {
        return logs.stream().map(ActivityLogDTO::from).collect(Collectors.toList());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChangedFields() { return changedFields; }
    public void setChangedFields(String changedFields) { this.changedFields = changedFields; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

