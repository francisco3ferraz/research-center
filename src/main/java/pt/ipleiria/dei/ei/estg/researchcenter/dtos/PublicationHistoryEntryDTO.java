package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.ActivityLog;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class PublicationHistoryEntryDTO implements Serializable {
    private Long id;
    private Long publicationId;
    private UserSummaryDTO editedBy;
    private String action;
    private List<String> changedFields;
    private OffsetDateTime timestamp;
    private String description;

    public PublicationHistoryEntryDTO() {}

    public PublicationHistoryEntryDTO(Long id, Long publicationId, UserSummaryDTO editedBy,
                                      String action, List<String> changedFields,
                                      OffsetDateTime timestamp, String description) {
        this.id = id;
        this.publicationId = publicationId;
        this.editedBy = editedBy;
        this.action = action;
        this.changedFields = changedFields;
        this.timestamp = timestamp;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPublicationId() { return publicationId; }
    public void setPublicationId(Long publicationId) { this.publicationId = publicationId; }
    public UserSummaryDTO getEditedBy() { return editedBy; }
    public void setEditedBy(UserSummaryDTO editedBy) { this.editedBy = editedBy; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public List<String> getChangedFields() { return changedFields; }
    public void setChangedFields(List<String> changedFields) { this.changedFields = changedFields; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static PublicationHistoryEntryDTO from(ActivityLog log) {
        if (log == null) return null;
        List<String> changed = List.of();
        if (log.getChangedFields() != null && !log.getChangedFields().isBlank()) {
            changed = java.util.Arrays.stream(log.getChangedFields().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }
        OffsetDateTime ts = log.getTimestamp() != null ? log.getTimestamp().atOffset(ZoneOffset.UTC) : null;
        return new PublicationHistoryEntryDTO(
                log.getId(),
                log.getEntityId(),
                log.getUser() != null ? UserSummaryDTO.from(log.getUser()) : null,
                log.getAction(),
                changed,
                ts,
                log.getDescription()
        );
    }
}

