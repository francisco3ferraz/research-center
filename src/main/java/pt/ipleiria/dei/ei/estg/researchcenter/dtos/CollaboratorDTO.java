package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.json.bind.annotation.JsonbProperty;

public class CollaboratorDTO implements Serializable {

    private Long id;
    private String username;
    private String name;
    private String email;
    private boolean active;
    private String role;
    @JsonbProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonbProperty("lastLogin")
    private LocalDateTime lastLogin;
    private List<TagDTO> subscribedTags;

    // Default constructor
    public CollaboratorDTO() {
    }

    // Constructor with parameters (without password for security)
    public CollaboratorDTO(String username, String name, String email, boolean active) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.active = active;
    }

    public CollaboratorDTO(Long id, String username, String name, String email, boolean active,
                           String role, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.active = active;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TagDTO> getSubscribedTags() {
        return subscribedTags;
    }

    public void setSubscribedTags(List<TagDTO> subscribedTags) {
        this.subscribedTags = subscribedTags;
    }

    // Conversion methods
    public static CollaboratorDTO from(Collaborator collaborator) {
        return new CollaboratorDTO(
            collaborator.getId(),
            collaborator.getUsername(),
            collaborator.getName(),
            collaborator.getEmail(),
            collaborator.isActive(),
            collaborator.getRole() != null ? collaborator.getRole().name() : null,
            collaborator.getCreatedAt(),
            collaborator.getLastLogin()
        );
    }

    public static CollaboratorDTO fromWithTags(Collaborator collaborator) {
        var dto = from(collaborator);
        dto.setSubscribedTags(TagDTO.from(collaborator.getSubscribedTags()));
        return dto;
    }

    public static List<CollaboratorDTO> from(List<Collaborator> collaborators) {
        return collaborators.stream()
                .map(CollaboratorDTO::from)
                .collect(Collectors.toList());
    }
}
