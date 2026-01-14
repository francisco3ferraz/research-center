package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Manager;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class ManagerBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public Manager create(String username, String password, String name, String email)
            throws MyEntityExistsException, MyConstraintViolationException {
        
        var existing = em.createQuery("SELECT m FROM Manager m WHERE m.username = :username", Manager.class)
                         .setParameter("username", username)
                         .getResultList();
        
        if (!existing.isEmpty()) {
            throw new MyEntityExistsException("Manager with username '" + username + "' already exists");
        }
        
        try {
            var manager = new Manager(username, password, name, email);
            em.persist(manager);
            em.flush();
            return manager;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public Manager find(Long id) throws MyEntityNotFoundException {
        var manager = em.find(Manager.class, id);
        if (manager == null) {
            throw new MyEntityNotFoundException("Manager with id " + id + " not found");
        }
        return manager;
    }
    
    public List<Manager> findAll() {
        return em.createQuery("SELECT m FROM Manager m ORDER BY m.name", Manager.class)
                 .getResultList();
    }
    
    public void update(Long id, String name, String email)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var manager = find(id);
        
        try {
            em.lock(manager, jakarta.persistence.LockModeType.OPTIMISTIC);
            manager.setName(name);
            manager.setEmail(email);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var manager = find(id);
        em.remove(manager);
    }
}