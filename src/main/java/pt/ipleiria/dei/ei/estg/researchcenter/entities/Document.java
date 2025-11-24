package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "documents")
@NamedQueries({
        @NamedQuery(
                name = "getPublicationDocument",
                query = "SELECT d FROM Document d WHERE d.publication.id = :publicationId"
        )
})
public class Document implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String filename;

    @NotBlank
    private String filepath;

    @OneToOne
    @NotNull
    private Publication publication;

    @Version
    private int version;

    // Default constructor
    public Document() {
    }

    // Constructor with parameters
    public Document(String filename, String filepath, Publication publication) {
        this.filename = filename;
        this.filepath = filepath;
        this.publication = publication;
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

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
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
}
