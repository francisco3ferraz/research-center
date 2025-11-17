package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_username", "publication_id"})
)
public class Rating implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    private int stars;

    @ManyToOne
    @NotNull
    private Collaborator user;

    @ManyToOne
    @NotNull
    private Publication publication;

    @Version
    private int version;

    // Default constructor
    public Rating() {
    }

    // Constructor with parameters
    public Rating(int stars, Collaborator user, Publication publication) {
        this.stars = stars;
        this.user = user;
        this.publication = publication;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public Collaborator getUser() {
        return user;
    }

    public void setUser(Collaborator user) {
        this.user = user;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}