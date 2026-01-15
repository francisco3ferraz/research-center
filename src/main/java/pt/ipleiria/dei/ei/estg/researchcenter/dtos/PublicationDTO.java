package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.CollaboratorDTO;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.json.bind.annotation.JsonbProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicationDTO implements Serializable {
    
    private Long id;
    private String title;
    private List<String> authors;
    private PublicationType type;
    private String areaScientific;
    private Integer year;
    private String publisher;
    private String doi;
    @JsonbProperty("abstract")
    @JsonProperty("abstract")
    private String abstract_;
    private String aiGeneratedSummary;
    private boolean visible;
    private boolean confidential;
    private OffsetDateTime uploadedAt;
    private OffsetDateTime updatedAt;
    private CollaboratorDTO uploadedBy;
    private List<TagDTO> tags;
    private List<CommentDTO> comments;
    private double averageRating;
    private int ratingsCount;
    private int commentsCount;
    private Long documentId;
    private String documentFilename;
    private String fileUrl;
    
    // Default constructor
    public PublicationDTO() {
    }
    
    // Constructor with parameters
    public PublicationDTO(Long id, String title, List<String> authors, PublicationType type,
                         String areaScientific, Integer year, String abstract_,
                         boolean visible, boolean confidential,
                         OffsetDateTime uploadedAt, CollaboratorDTO uploadedBy) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.type = type;
        this.areaScientific = areaScientific;
        this.year = year;
        this.abstract_ = abstract_;
        this.visible = visible;
        this.confidential = confidential;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
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
    
    @JsonProperty("abstract")
    public String getAbstract_() {
        return abstract_;
    }
    
    @JsonProperty("abstract")
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
    
    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CollaboratorDTO getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(CollaboratorDTO uploadedBy) {
        this.uploadedBy = uploadedBy;
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
    
    public int getRatingsCount() {
        return ratingsCount;
    }
    
    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }
    
    public int getCommentsCount() {
        return commentsCount;
    }
    
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    // Conversion methods
    public static PublicationDTO from(Publication publication) {
        OffsetDateTime up = null;
        OffsetDateTime upd = null;
        if (publication.getUploadedAt() != null) {
            up = publication.getUploadedAt().atOffset(ZoneOffset.UTC);
        }
        if (publication.getUpdatedAt() != null) {
            upd = publication.getUpdatedAt().atOffset(ZoneOffset.UTC);
        }
        var dto = new PublicationDTO(
            publication.getId(),
            publication.getTitle(),
            publication.getAuthors(),
            publication.getType(),
            publication.getAreaScientific(),
            publication.getYear(),
            publication.getAbstract_(),
            publication.isVisible(),
            publication.isConfidential(),
            up,
            publication.getUploadedBy() != null ? CollaboratorDTO.from(publication.getUploadedBy()) : null
        );

        dto.setPublisher(publication.getPublisher());
        dto.setDoi(publication.getDoi());
        dto.setAiGeneratedSummary(publication.getAiGeneratedSummary());
        dto.setUpdatedAt(upd);
        dto.setAverageRating(publication.getAverageRating());
        dto.setRatingsCount(publication.getRatings().size());
        dto.setCommentsCount(publication.getComments().size());

        if (publication.getDocument() != null) {
            dto.setDocumentId(publication.getDocument().getId());
            dto.setDocumentFilename(publication.getDocument().getFilename());
            dto.setFileUrl("/api/publications/" + publication.getId() + "/file");
        }

        return dto;
    }
    
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