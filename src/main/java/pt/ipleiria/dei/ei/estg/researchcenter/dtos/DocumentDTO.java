package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Document;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentDTO implements Serializable {

    private Long id;
    private String filename;
    private Long publicationId;

    // Default constructor
    public DocumentDTO() {
    }

    // Constructor with parameters
    public DocumentDTO(Long id, String filename, Long publicationId) {
        this.id = id;
        this.filename = filename;
        this.publicationId = publicationId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    // Conversion methods
    public static DocumentDTO from(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getFilename(),
                document.getPublication().getId()
        );
    }

    public static List<DocumentDTO> from(List<Document> documents) {
        return documents.stream()
                .map(DocumentDTO::from)
                .collect(Collectors.toList());
    }
}
