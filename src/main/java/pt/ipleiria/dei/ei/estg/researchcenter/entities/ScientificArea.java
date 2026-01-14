package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scientific_areas")
@NamedQueries({
        @NamedQuery(
                name = "getAllScientificAreas",
                query = "SELECT sa FROM ScientificArea sa ORDER BY sa.name"
        )
})
public class ScientificArea implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "area", cascade = CascadeType.REMOVE)
    private List<Publication> publications;

    @Version
    private int version;

    // Default constructor
    public ScientificArea() {
        this.publications = new ArrayList<>();
    }

    // Constructor with parameters
    public ScientificArea(String name) {
        this.name = name;
        this.publications = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
}
