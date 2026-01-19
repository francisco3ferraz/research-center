package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.PersistenceException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Stateless
public class TagBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public Tag create(String name, String description) 
            throws MyEntityExistsException, MyConstraintViolationException {
        
        // Check if tag already exists
        var existingTag = em.createQuery(
            "SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
            .setParameter("name", name)
            .getResultList();
        
        if (!existingTag.isEmpty()) {
            throw new MyEntityExistsException("Tag '" + name + "' already exists");
        }
        
        try {
            var tag = new Tag(name, description);
            em.persist(tag);
            em.flush();
            return tag;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        } catch (PersistenceException pe) {
            // Database-level constraint (e.g. unique index) may throw a PersistenceException wrapping
            // a vendor-specific ConstraintViolationException. Convert to a friendly MyEntityExistsException
            // when it looks like a uniqueness violation, otherwise wrap as MyConstraintViolationException.
            String msg = pe.getMessage() != null ? pe.getMessage().toLowerCase() : "";
            Throwable cause = pe.getCause();
            String causeName = cause != null ? cause.getClass().getName().toLowerCase() : "";

            if (msg.contains("constraint") || msg.contains("unique") || causeName.contains("constraintviolation") || causeName.contains("constraintviolationexception") || causeName.contains("constraintviolation")) {
                throw new MyEntityExistsException("Tag '" + name + "' already exists");
            }

            throw new MyConstraintViolationException(pe);
        }
    }
    
    public Tag find(Long id) throws MyEntityNotFoundException {
        var tag = em.find(Tag.class, id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag with id " + id + " not found");
        }
        // Initialize lazy collections while entity is managed
        tag.getPublications().size();
        tag.getSubscribers().size();
        return tag;
    }
    
    public Tag findByName(String name) throws MyEntityNotFoundException {
        var tags = em.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                     .setParameter("name", name)
                     .getResultList();
        
        if (tags.isEmpty()) {
            throw new MyEntityNotFoundException("Tag '" + name + "' not found");
        }
        var tag = tags.get(0);
        // Initialize lazy collections
        tag.getPublications().size();
        tag.getSubscribers().size();
        return tag;
    }
    
    public List<Tag> findAll() {
        var list = em.createNamedQuery("getAllTags", Tag.class).getResultList();
        // Initialize collections for each tag
        for (var t : list) {
            t.getPublications().size();
            t.getSubscribers().size();
        }
        return list;
    }
    
    public List<Tag> findAllVisible() {
        var list = em.createQuery("SELECT t FROM Tag t WHERE t.visible = true ORDER BY t.name", Tag.class)
                 .getResultList();
        for (var t : list) {
            t.getPublications().size();
            t.getSubscribers().size();
        }
        return list;
    }
    
    public List<Tag> findAllHidden() {
        var list = em.createQuery("SELECT t FROM Tag t WHERE t.visible = false ORDER BY t.name", Tag.class)
                 .getResultList();
        for (var t : list) {
            t.getPublications().size();
            t.getSubscribers().size();
        }
        return list;
    }
    
    public void update(Long id, String name, String description) 
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var tag = find(id);
        
        try {
            em.lock(tag, jakarta.persistence.LockModeType.OPTIMISTIC);
            tag.setName(name);
            tag.setDescription(description);
            tag.setUpdatedAt(LocalDateTime.now());
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void setVisibility(Long id, boolean visible) throws MyEntityNotFoundException {
        var tag = find(id);
        tag.setVisible(visible);
        tag.setUpdatedAt(LocalDateTime.now());
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var tag = find(id);

        // Detach this tag from all publications
        var pubs = new ArrayList<Publication>(tag.getPublications());
        for (Publication p : pubs) {
            p.getTags().remove(tag);
            em.merge(p);
        }

        // Detach this tag from all subscribers
        var subs = new ArrayList<Collaborator>(tag.getSubscribers());
        for (Collaborator c : subs) {
            c.getSubscribedTags().remove(tag);
            em.merge(c);
        }

        // Clear associations on tag side
        tag.getPublications().clear();
        tag.getSubscribers().clear();

        // Now safe to remove
        if (!em.contains(tag)) tag = em.merge(tag);
        em.remove(tag);
        em.flush();
    }
}