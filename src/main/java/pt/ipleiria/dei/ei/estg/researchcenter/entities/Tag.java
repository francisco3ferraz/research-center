package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@NamedQueries({
        @NamedQuery(
                name = "getAllTags",
                query = "SELECT t FROM Tag t ORDER BY t.name"
        )
})
public class Tag implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<Publication> publications;

    @ManyToMany
    @JoinTable(
            name = "tag_subscribers",
            joinColumns = @JoinColumn(name = "tag_id"),
            inverseJoinColumns = @JoinColumn(name = "user_username")
    )
    private List<Collaborator> subscribers;

    @Version
    private int version;

    // Default constructor
    public Tag() {
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    // Constructor with parameters
    public Tag(String name) {
        this.name = name;
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
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

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<Collaborator> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Collaborator> subscribers) {
        this.subscribers = subscribers;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // Helper methods
    public void addPublication(Publication publication) {
        if (!publications.contains(publication)) {
            publications.add(publication);
        }
    }

    public void removePublication(Publication publication) {
        publications.remove(publication);
    }

    public void addSubscriber(Collaborator collaborator) {
        if (!subscribers.contains(collaborator)) {
            subscribers.add(collaborator);
        }
    }

    public void removeSubscriber(Collaborator collaborator) {
        subscribers.remove(collaborator);
    }
}