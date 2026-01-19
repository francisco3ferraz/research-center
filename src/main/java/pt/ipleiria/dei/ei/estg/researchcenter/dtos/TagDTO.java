package pt.ipleiria.dei.ei.estg.researchcenter.dtos;


import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.stream.Collectors;

public class TagDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private boolean visible;
    @JsonbProperty("createdAt")
    private OffsetDateTime createdAt;
    @JsonbProperty("updatedAt")
    private OffsetDateTime updatedAt;
    @JsonbProperty("publicationsCount")
    private int publicationsCount;

    // Default constructor
    public TagDTO() {
    }

    // Constructor with parameters
    public TagDTO(Long id, String name, String description, boolean visible,
                  OffsetDateTime createdAt, OffsetDateTime updatedAt, int publicationsCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.visible = visible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.publicationsCount = publicationsCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getPublicationsCount() {
        return publicationsCount;
    }

    public void setPublicationsCount(int publicationsCount) {
        this.publicationsCount = publicationsCount;
    }

    // Conversion methods
    public static TagDTO from(Tag tag) {
        OffsetDateTime createdAt = tag.getCreatedAt() != null ? tag.getCreatedAt().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime updatedAt = tag.getUpdatedAt() != null ? tag.getUpdatedAt().atOffset(ZoneOffset.UTC) : null;
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getDescription(),
                tag.isVisible(),
                createdAt,
                updatedAt,
                tag.getPublications().size()
        );
    }

    public static List<TagDTO> from(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::from)
                .collect(Collectors.toList());
    }

    // Deprecated: Publication responses use TagSimpleDTO instead.
}