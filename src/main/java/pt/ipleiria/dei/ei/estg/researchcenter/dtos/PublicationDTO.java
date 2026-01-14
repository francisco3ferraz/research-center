package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {

    private Long id;
    private String title;
    private String summary;
    private LocalDateTime uploadDate;
    private boolean visible;
    private String submitterUsername;
    private String submitterName;
    private Long areaId;
    private String areaName;
    private List<TagDTO> tags;
    private List<CommentDTO> comments;
    private double averageRating;
    private int totalRatings;
    private int totalComments;
    private Long documentId;
    private String documentFilename;

    // Default constructor
    public PublicationDTO() {
    }

    // Constructor with parameters
    public PublicationDTO(Long id, String title, String summary, LocalDateTime uploadDate,
                          boolean visible, String submitterUsername, String submitterName,
                          Long areaId, String areaName) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.uploadDate = uploadDate;
        this.visible = visible;
        this.submitterUsername = submitterUsername;
        this.submitterName = submitterName;
        this.areaId = areaId;
        this.areaName = areaName;
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

    public String getSubmitterUsername() {
        return submitterUsername;
    }

    public void setSubmitterUsername(String submitterUsername) {
        this.submitterUsername = submitterUsername;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentFilename() {
        return documentFilename;
    }

    public void setDocumentFilename(String documentFilename) {
        this.documentFilename = documentFilename;
    }

    // Conversion methods

    // Simple conversion (for lists)
    public static PublicationDTO from(Publication publication) {
        var dto = new PublicationDTO(
                publication.getId(),
                publication.getTitle(),
                publication.getSummary(),
                publication.getUploadDate(),
                publication.isVisible(),
                publication.getSubmitter().getUsername(),
                publication.getSubmitter().getName(),
                publication.getArea().getId(),
                publication.getArea().getName()
        );

        dto.setAverageRating(publication.getAverageRating());
        dto.setTotalRatings(publication.getRatings().size());
        dto.setTotalComments(publication.getComments().size());

        if (publication.getDocument() != null) {
            dto.setDocumentId(publication.getDocument().getId());
            dto.setDocumentFilename(publication.getDocument().getFilename());
        }

        return dto;
    }

    // Detailed conversion (for single publication view)
    public static PublicationDTO fromWithDetails(Publication publication) {
        var dto = from(publication);
        dto.setTags(TagDTO.fromSimple(publication.getTags()));
        dto.setComments(CommentDTO.from(
                publication.getComments().stream()
                        .filter(c -> c.isVisible())
                        .collect(Collectors.toList())
        ));
        return dto;
    }

    public static List<PublicationDTO> from(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::from)
                .collect(Collectors.toList());
    }
}
