package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorDTO implements Serializable {

    private String username;
    private String name;
    private String email;
    private boolean active;
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

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
                collaborator.getUsername(),
                collaborator.getName(),
                collaborator.getEmail(),
                collaborator.isActive()
        );
    }

    public static CollaboratorDTO fromWithTags(Collaborator collaborator) {
        var dto = from(collaborator);
        dto.setSubscribedTags(TagDTO.fromSimple(collaborator.getSubscribedTags()));
        return dto;
    }

    public static List<CollaboratorDTO> from(List<Collaborator> collaborators) {
        return collaborators.stream()
                .map(CollaboratorDTO::from)
                .collect(Collectors.toList());
    }
}
