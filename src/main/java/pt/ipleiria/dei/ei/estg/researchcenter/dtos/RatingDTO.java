package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Rating;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.json.bind.annotation.JsonbProperty;

public class RatingDTO implements Serializable {

    private Long id;
    private Long publicationId;
    private Long userId;
    private String userName;
    @JsonbProperty("value")
    private int value;
    private OffsetDateTime createdAt;

    // Default constructor
    public RatingDTO() {
    }

    // Constructor with parameters
    public RatingDTO(Long id, Long publicationId, Long userId, String userName, int value, OffsetDateTime createdAt) {
        this.id = id;
        this.publicationId = publicationId;
        this.userId = userId;
        this.userName = userName;
        this.value = value;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    // Conversion methods
    public static RatingDTO from(Rating rating) {
        OffsetDateTime createdAt = null;
        if (rating.getCreatedAt() != null) {
            createdAt = rating.getCreatedAt().atOffset(ZoneOffset.UTC);
        }
        return new RatingDTO(
                rating.getId(),
                rating.getPublication().getId(),
                rating.getUser().getId(),
                rating.getUser().getName(),
                rating.getStars(),
                createdAt
        );
    }

    public static List<RatingDTO> from(List<Rating> ratings) {
        return ratings.stream()
                .map(RatingDTO::from)
                .collect(Collectors.toList());
    }
}
