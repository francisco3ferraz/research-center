package pt.ipleiria.dei.ei.estg.researchcenter.dtos;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal tag representation (id + name) used inside publication responses.
 */
public class TagSimpleDTO implements Serializable {
    private Long id;
    private String name;

    public TagSimpleDTO() {}

    public TagSimpleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public static TagSimpleDTO from(Tag tag) {
        if (tag == null) return null;
        return new TagSimpleDTO(tag.getId(), tag.getName());
    }

    public static List<TagSimpleDTO> from(List<Tag> tags) {
        if (tags == null) return List.of();
        return tags.stream().map(TagSimpleDTO::from).collect(Collectors.toList());
    }
}

