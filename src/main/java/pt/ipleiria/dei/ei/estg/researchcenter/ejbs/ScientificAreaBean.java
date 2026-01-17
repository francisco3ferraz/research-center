package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.ScientificArea;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class ScientificAreaBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public ScientificArea create(String name, String description) 
            throws MyEntityExistsException, MyConstraintViolationException {
        // Check if area with same name already exists
        var existing = em.createQuery(
            "SELECT sa FROM ScientificArea sa WHERE LOWER(sa.name) = LOWER(:name)", 
            ScientificArea.class)
            .setParameter("name", name)
            .getResultList();
        
        if (!existing.isEmpty()) {
            throw new MyEntityExistsException("Área científica '" + name + "' já existe");
        }
        
        try {
            var area = new ScientificArea(name);
            area.setDescription(description);
            em.persist(area);
            em.flush();
            return area;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public ScientificArea find(Long id) throws MyEntityNotFoundException {
        var area = em.find(ScientificArea.class, id);
        if (area == null) {
            throw new MyEntityNotFoundException("Área científica com id " + id + " não encontrada");
        }
        // Initialize lazy collection to avoid LazyInitializationException
        Hibernate.initialize(area.getPublications());
        return area;
    }
    
    public List<ScientificArea> findAll() {
        var areas = em.createNamedQuery("getAllScientificAreas", ScientificArea.class)
                 .getResultList();
        // Initialize lazy collections to avoid LazyInitializationException
        for (ScientificArea area : areas) {
            Hibernate.initialize(area.getPublications());
        }
        return areas;
    }
    
    public ScientificArea update(Long id, String name, String description) 
            throws MyEntityNotFoundException, MyConstraintViolationException, MyEntityExistsException {
        var area = em.find(ScientificArea.class, id);
        if (area == null) {
            throw new MyEntityNotFoundException("Área científica com id " + id + " não encontrada");
        }
        
        // Check if another area with same name already exists
        var existing = em.createQuery(
            "SELECT sa FROM ScientificArea sa WHERE LOWER(sa.name) = LOWER(:name) AND sa.id != :id", 
            ScientificArea.class)
            .setParameter("name", name)
            .setParameter("id", id)
            .getResultList();
        
        if (!existing.isEmpty()) {
            throw new MyEntityExistsException("Área científica '" + name + "' já existe");
        }
        
        try {
            area.setName(name);
            if (description != null) {
                area.setDescription(description);
            }
            em.flush();
            // Initialize lazy collection to avoid LazyInitializationException
            Hibernate.initialize(area.getPublications());
            return area;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var area = em.find(ScientificArea.class, id);
        if (area == null) {
            throw new MyEntityNotFoundException("Área científica com id " + id + " não encontrada");
        }
        em.remove(area);
    }
    
    public long countPublications(Long areaId) {
        return em.createQuery(
            "SELECT COUNT(p) FROM Publication p WHERE p.area.id = :areaId", Long.class)
            .setParameter("areaId", areaId)
            .getSingleResult();
    }
}
