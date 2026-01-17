package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.ScientificArea;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScientificAreaDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private int publicationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public ScientificAreaDTO() {
    }

    // Constructor with parameters
    public ScientificAreaDTO(Long id, String name, String description, int publicationsCount) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPublicationsCount() {
        return publicationsCount;
    }

    public void setPublicationsCount(int publicationsCount) {
        this.publicationsCount = publicationsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Conversion methods
    public static ScientificAreaDTO from(ScientificArea area) {
        return new ScientificAreaDTO(
                area.getId(),
                area.getName(),
                area.getDescription(),
                area.getPublications() != null ? area.getPublications().size() : 0
        );
    }

    public static List<ScientificAreaDTO> from(List<ScientificArea> areas) {
        return areas.stream()
                .map(ScientificAreaDTO::from)
                .collect(Collectors.toList());
    }
}