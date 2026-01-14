package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Comment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO implements Serializable {

    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private boolean visible;
    private String authorUsername;
    private String authorName;
    private Long publicationId;

    // Default constructor
    public CommentDTO() {
    }

    // Constructor with parameters
    public CommentDTO(Long id, String text, LocalDateTime createdAt, boolean visible,
                      String authorUsername, String authorName, Long publicationId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.visible = visible;
        this.authorUsername = authorUsername;
        this.authorName = authorName;
        this.publicationId = publicationId;
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

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    // Conversion methods
    public static CommentDTO from(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.isVisible(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getName(),
                comment.getPublication().getId()
        );
    }

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentDTO::from)
                .collect(Collectors.toList());
    }
}
