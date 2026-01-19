package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;

import java.io.Serializable;

/**
 * Minimal user representation for nested objects in responses (id + name),
 * as described throughout the API spec PDFs.
 */
public class UserSummaryDTO implements Serializable {
    private Long id;
    private String name;

    public UserSummaryDTO() {}

    public UserSummaryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static UserSummaryDTO from(User user) {
        if (user == null) return null;
        return new UserSummaryDTO(user.getId(), user.getName());
    }

    public static UserSummaryDTO from(Collaborator collaborator) {
        if (collaborator == null) return null;
        return new UserSummaryDTO(collaborator.getId(), collaborator.getName());
    }
}

