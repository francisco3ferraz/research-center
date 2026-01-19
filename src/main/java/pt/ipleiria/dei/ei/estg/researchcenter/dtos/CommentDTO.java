package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Comment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO implements Serializable {

    private Long id;
    @jakarta.json.bind.annotation.JsonbProperty("content")
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean visible;
    private UserSummaryDTO author;
    private Long publicationId;

    // Default constructor
    public CommentDTO() {
    }

    // Constructor with parameters
    public CommentDTO(Long id, String text, LocalDateTime createdAt, LocalDateTime updatedAt, boolean visible,
                      UserSummaryDTO author, Long publicationId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.visible = visible;
        this.author = author;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public UserSummaryDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserSummaryDTO author) {
        this.author = author;
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
            comment.getUpdatedAt(),
            comment.isVisible(),
            UserSummaryDTO.from(comment.getAuthor()),
            comment.getPublication().getId()
        );
    }

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentDTO::from)
                .collect(Collectors.toList());
    }
}
