package pt.ipleiria.dei.ei.estg.researchcenter.dtos;


import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class TagDTO implements Serializable {

    private Long id;
    private String name;
    private int publicationCount;
    private int subscriberCount;

    // Default constructor
    public TagDTO() {
    }

    // Constructor with parameters
    public TagDTO(Long id, String name, int publicationCount, int subscriberCount) {
        this.id = id;
        this.name = name;
        this.publicationCount = publicationCount;
        this.subscriberCount = subscriberCount;
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

    public int getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(int publicationCount) {
        this.publicationCount = publicationCount;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    // Conversion methods
    public static TagDTO from(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getPublications().size(),
                tag.getSubscribers().size()
        );
    }

    public static List<TagDTO> from(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::from)
                .collect(Collectors.toList());
    }

    // Simple version without counts (for lists inside other DTOs)
    public static TagDTO fromSimple(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName(), 0, 0);
    }

    public static List<TagDTO> fromSimple(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::fromSimple)
                .collect(Collectors.toList());
    }
}