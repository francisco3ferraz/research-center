package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.ScientificArea;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class ScientificAreaDTO implements Serializable {

    private Long id;
    private String name;

    // Default constructor
    public ScientificAreaDTO() {
    }

    // Constructor with parameters
    public ScientificAreaDTO(Long id, String name) {
        this.id = id;
        this.name = name;
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

    // Conversion methods
    public static ScientificAreaDTO from(ScientificArea area) {
        return new ScientificAreaDTO(
                area.getId(),
                area.getName()
        );
    }

    public static List<ScientificAreaDTO> from(List<ScientificArea> areas) {
        return areas.stream()
                .map(ScientificAreaDTO::from)
                .collect(Collectors.toList());
    }
}