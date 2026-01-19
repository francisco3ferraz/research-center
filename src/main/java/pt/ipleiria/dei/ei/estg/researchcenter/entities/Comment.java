package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(length = 1000)
    private String text;
    
    @NotNull
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean visible = true;

    @ManyToOne
    @JoinColumn(name = "hidden_by_user_id")
    private User hiddenBy;

    private LocalDateTime hiddenAt;
    
    @ManyToOne
    @NotNull
    private Collaborator author;
    
    @ManyToOne
    @NotNull
    private Publication publication;
    
    @Version
    private int version;
    
    // Default constructor
    public Comment() {
    }
    
    // Constructor with parameters
    public Comment(String text, Collaborator author, Publication publication) {
        this.text = text;
        this.author = author;
        this.publication = publication;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public Collaborator getAuthor() {
        return author;
    }
    
    public void setAuthor(Collaborator author) {
        this.author = author;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
}