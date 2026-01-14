package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

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
        }
    }
    
    public Tag find(Long id) throws MyEntityNotFoundException {
        var tag = em.find(Tag.class, id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag with id " + id + " not found");
        }
        return tag;
    }
    
    public Tag findByName(String name) throws MyEntityNotFoundException {
        var tags = em.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class)
                     .setParameter("name", name)
                     .getResultList();
        
        if (tags.isEmpty()) {
            throw new MyEntityNotFoundException("Tag '" + name + "' not found");
        }
        return tags.get(0);
    }
    
    public List<Tag> findAll() {
        return em.createNamedQuery("getAllTags", Tag.class).getResultList();
    }
    
    public List<Tag> findAllVisible() {
        return em.createQuery("SELECT t FROM Tag t WHERE t.visible = true ORDER BY t.name", Tag.class)
                 .getResultList();
    }
    
    public List<Tag> findAllHidden() {
        return em.createQuery("SELECT t FROM Tag t WHERE t.visible = false ORDER BY t.name", Tag.class)
                 .getResultList();
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
        em.remove(tag);
    }
}