package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CollaboratorBean {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private TagBean tagBean;
    
    public Collaborator create(String username, String password, String name, String email)
            throws MyEntityExistsException, MyConstraintViolationException {
        
        // Check if username already exists
        var existing = em.createQuery("SELECT c FROM Collaborator c WHERE c.username = :username", Collaborator.class)
                         .setParameter("username", username)
                         .getResultList();
        
        if (!existing.isEmpty()) {
            throw new MyEntityExistsException("Collaborator with username '" + username + "' already exists");
        }
        
        try {
            var collaborator = new Collaborator(username, password, name, email);
            em.persist(collaborator);
            em.flush();
            return collaborator;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public Collaborator find(Long id) throws MyEntityNotFoundException {
        var collaborator = em.find(Collaborator.class, id);
        if (collaborator == null) {
            throw new MyEntityNotFoundException("Collaborator with id " + id + " not found");
        }
        return collaborator;
    }
    
    public Collaborator findByUsername(String username) throws MyEntityNotFoundException {
        var collaborators = em.createQuery("SELECT c FROM Collaborator c WHERE c.username = :username", Collaborator.class)
                             .setParameter("username", username)
                             .getResultList();
        
        if (collaborators.isEmpty()) {
            throw new MyEntityNotFoundException("Collaborator '" + username + "' not found");
        }
        return collaborators.get(0);
    }
    
    public List<Collaborator> findAll() {
        return em.createQuery("SELECT c FROM Collaborator c ORDER BY c.name", Collaborator.class)
                 .getResultList();
    }
    
    public void update(Long id, String name, String email)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var collaborator = find(id);
        
        try {
            em.lock(collaborator, jakarta.persistence.LockModeType.OPTIMISTIC);
            collaborator.setName(name);
            collaborator.setEmail(email);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void updatePassword(Long id, String password)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var collaborator = find(id);
        
        try {
            em.lock(collaborator, jakarta.persistence.LockModeType.OPTIMISTIC);
            collaborator.setPassword(password);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void setActive(Long id, boolean active) throws MyEntityNotFoundException {
        var collaborator = find(id);
        collaborator.setActive(active);
    }
    
    public void updateLastLogin(Long id) throws MyEntityNotFoundException {
        var collaborator = find(id);
        collaborator.setLastLogin(LocalDateTime.now());
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var collaborator = find(id);
        em.remove(collaborator);
    }
    
    public void subscribeToTag(Long collaboratorId, Long tagId) 
            throws MyEntityNotFoundException {
        var collaborator = find(collaboratorId);
        var tag = tagBean.find(tagId);
        
        collaborator.addSubscribedTag(tag);
        tag.addSubscriber(collaborator);
    }
    
    public void unsubscribeFromTag(Long collaboratorId, Long tagId) 
            throws MyEntityNotFoundException {
        var collaborator = find(collaboratorId);
        var tag = tagBean.find(tagId);
        
        collaborator.removeSubscribedTag(tag);
        tag.removeSubscriber(collaborator);
    }
}