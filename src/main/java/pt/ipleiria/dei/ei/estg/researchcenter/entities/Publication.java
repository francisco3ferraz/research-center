package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publications")
@NamedQueries({
        @NamedQuery(
                name = "getAllPublications",
                query = "SELECT p FROM Publication p WHERE p.visible = true ORDER BY p.uploadDate DESC"
        ),
        @NamedQuery(
                name = "getAllPublicationsIncludingHidden",
                query = "SELECT p FROM Publication p ORDER BY p.uploadDate DESC"
        )
})
public class Publication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String summary;

    @NotNull
    private LocalDateTime uploadDate;

    @OneToOne(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private Document document;

    private boolean visible = true;

    @ManyToOne
    @NotNull
    private Collaborator submitter;

    @ManyToOne
    @NotNull
    private ScientificArea area;

    @ManyToMany
    @JoinTable(
            name = "publication_tags",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    @Version
    private int version;

    // Default constructor
    public Publication() {
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    // Constructor with parameters
    public Publication(String title, String summary, Collaborator submitter, ScientificArea area) {
        this.title = title;
        this.summary = summary;
        this.uploadDate = LocalDateTime.now();
        this.submitter = submitter;
        this.area = area;
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Collaborator getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Collaborator submitter) {
        this.submitter = submitter;
    }

    public ScientificArea getArea() {
        return area;
    }

    public void setArea(ScientificArea area) {
        this.area = area;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // Helper methods
    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void addComment(Comment comment) {
        if (!comments.contains(comment)) {
            comments.add(comment);
        }
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
    }

    public void addRating(Rating rating) {
        if (!ratings.contains(rating)) {
            ratings.add(rating);
        }
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
    }

    // Calculate average rating
    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);
    }
}