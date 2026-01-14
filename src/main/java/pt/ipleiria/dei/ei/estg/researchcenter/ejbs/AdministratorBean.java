package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Administrator;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class AdministratorBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public Administrator create(String username, String password, String name, String email)
            throws MyEntityExistsException, MyConstraintViolationException {
        
        var existing = em.createQuery("SELECT a FROM Administrator a WHERE a.username = :username", Administrator.class)
                         .setParameter("username", username)
                         .getResultList();
        
        if (!existing.isEmpty()) {
            throw new MyEntityExistsException("Administrator with username '" + username + "' already exists");
        }
        
        try {
            var admin = new Administrator(username, password, name, email);
            em.persist(admin);
            em.flush();
            return admin;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public Administrator find(Long id) throws MyEntityNotFoundException {
        var admin = em.find(Administrator.class, id);
        if (admin == null) {
            throw new MyEntityNotFoundException("Administrator with id " + id + " not found");
        }
        return admin;
    }
    
    public List<Administrator> findAll() {
        return em.createQuery("SELECT a FROM Administrator a ORDER BY a.name", Administrator.class)
                 .getResultList();
    }
    
    public void update(Long id, String name, String email)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var admin = find(id);
        
        try {
            em.lock(admin, jakarta.persistence.LockModeType.OPTIMISTIC);
            admin.setName(name);
            admin.setEmail(email);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var admin = find(id);
        em.remove(admin);
    }
}