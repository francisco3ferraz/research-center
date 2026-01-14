package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    
    @Column(length = 500)
    private String description;
    
    private boolean visible = true;
    
    @NotNull
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
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
        this.createdAt = LocalDateTime.now();
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }
    
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
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