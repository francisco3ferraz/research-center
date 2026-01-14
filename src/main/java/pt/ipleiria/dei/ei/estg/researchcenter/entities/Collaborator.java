package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Collaborator extends User {
    
    @OneToMany(mappedBy = "submitter", cascade = CascadeType.REMOVE)
    private List<Publication> publications;
    
    @ManyToMany(mappedBy = "subscribers")
    private List<Tag> subscribedTags;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Comment> comments;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Rating> ratings;
    
    // Default constructor
    public Collaborator() {
        this.publications = new ArrayList<>();
        this.subscribedTags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }
    
    // Constructor with parameters
    public Collaborator(String username, String password, String name, String email) {
        super(username, password, name, email, UserRole.COLABORADOR);
        this.publications = new ArrayList<>();
        this.subscribedTags = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }
    
    // Getters and Setters
    public List<Publication> getPublications() {
        return publications;
    }
    
    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
    
    public List<Tag> getSubscribedTags() {
        return subscribedTags;
    }
    
    public void setSubscribedTags(List<Tag> subscribedTags) {
        this.subscribedTags = subscribedTags;
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
    
    // Helper methods
    public void addPublication(Publication publication) {
        if (!publications.contains(publication)) {
            publications.add(publication);
        }
    }
    
    public void removePublication(Publication publication) {
        publications.remove(publication);
    }
    
    public void addSubscribedTag(Tag tag) {
        if (!subscribedTags.contains(tag)) {
            subscribedTags.add(tag);
        }
    }
    
    public void removeSubscribedTag(Tag tag) {
        subscribedTags.remove(tag);
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
}