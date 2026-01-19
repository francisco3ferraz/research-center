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
        query = "SELECT p FROM Publication p WHERE p.visible = true ORDER BY p.uploadedAt DESC"
    ),
    @NamedQuery(
        name = "getAllPublicationsIncludingHidden",
        query = "SELECT p FROM Publication p ORDER BY p.uploadedAt DESC"
    )
})
public class Publication implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String title;
    
    @ElementCollection
    @CollectionTable(name = "publication_authors", joinColumns = @JoinColumn(name = "publication_id"))
    @Column(name = "author_name")
    private List<String> authors;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private PublicationType type;
    
    @NotBlank
    @Column(name = "area_scientific")
    private String areaScientific;
    
    @NotNull
    private Integer year;
    
    private String publisher;
    
    private String doi;
    
    @Column(length = 2000)
    private String abstract_;
    
    @Column(length = 2000)
    private String aiGeneratedSummary;
    
    private boolean visible = true;
    
    private boolean confidential = false;

    // Total number of views for this publication (used by statistics/top-publications)
    private long viewsCount = 0;

    @ManyToOne
    @JoinColumn(name = "hidden_by_user_id")
    private User hiddenBy;

    private LocalDateTime hiddenAt;
    
    @NotNull
    private LocalDateTime uploadedAt;
    
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by_id")
    private Collaborator uploadedBy;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private ScientificArea area;

    @ManyToOne
    @JoinColumn(name = "submitter_id")
    private Collaborator submitter;
    
    @OneToOne(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
    private Document document;
    
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
        this.authors = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }
    
    // Constructor with parameters
    public Publication(String title, List<String> authors, PublicationType type, 
                      String areaScientific, Integer year, String abstract_, 
                      Collaborator uploadedBy) {
        this.title = title;
        this.authors = authors != null ? new ArrayList<>(authors) : new ArrayList<>();
        this.type = type;
        this.areaScientific = areaScientific;
        this.year = year;
        this.abstract_ = abstract_;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = LocalDateTime.now();
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
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    
    public PublicationType getType() {
        return type;
    }
    
    public void setType(PublicationType type) {
        this.type = type;
    }
    
    public String getAreaScientific() {
        return areaScientific;
    }
    
    public void setAreaScientific(String areaScientific) {
        this.areaScientific = areaScientific;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getDoi() {
        return doi;
    }
    
    public void setDoi(String doi) {
        this.doi = doi;
    }
    
    public String getAbstract_() {
        return abstract_;
    }
    
    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }
    
    public String getAiGeneratedSummary() {
        return aiGeneratedSummary;
    }
    
    public void setAiGeneratedSummary(String aiGeneratedSummary) {
        this.aiGeneratedSummary = aiGeneratedSummary;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isConfidential() {
        return confidential;
    }
    
    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public void incrementViews() {
        this.viewsCount++;
    }

    public User getHiddenBy() {
        return hiddenBy;
    }

    public void setHiddenBy(User hiddenBy) {
        this.hiddenBy = hiddenBy;
    }

    public LocalDateTime getHiddenAt() {
        return hiddenAt;
    }

    public void setHiddenAt(LocalDateTime hiddenAt) {
        this.hiddenAt = hiddenAt;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Collaborator getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(Collaborator uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public ScientificArea getArea() {
        return area;
    }

    public void setArea(ScientificArea area) {
        this.area = area;
    }

    public Collaborator getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Collaborator submitter) {
        this.submitter = submitter;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
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
    
    // Add author helper
    public void addAuthor(String authorName) {
        if (!authors.contains(authorName)) {
            authors.add(authorName);
        }
    }
    
    public void removeAuthor(String authorName) {
        authors.remove(authorName);
    }
}