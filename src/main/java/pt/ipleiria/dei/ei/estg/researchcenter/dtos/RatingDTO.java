package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Rating;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class RatingDTO implements Serializable {

    private Long id;
    private int stars;
    private String userUsername;
    private String userName;
    private Long publicationId;

    // Default constructor
    public RatingDTO() {
    }

    // Constructor with parameters
    public RatingDTO(Long id, int stars, String userUsername, String userName, Long publicationId) {
        this.id = id;
        this.stars = stars;
        this.userUsername = userUsername;
        this.userName = userName;
        this.publicationId = publicationId;
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

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    // Conversion methods
    public static RatingDTO from(Rating rating) {
        return new RatingDTO(
                rating.getId(),
                rating.getStars(),
                rating.getUser().getUsername(),
                rating.getUser().getName(),
                rating.getPublication().getId()
        );
    }

    public static List<RatingDTO> from(List<Rating> ratings) {
        return ratings.stream()
                .map(RatingDTO::from)
                .collect(Collectors.toList());
    }
}
