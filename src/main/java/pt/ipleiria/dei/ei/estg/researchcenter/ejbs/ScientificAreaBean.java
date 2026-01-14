package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.ScientificArea;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class ScientificAreaBean {
    @PersistenceContext
    private EntityManager em;

    public ScientificArea create(String name)
            throws MyEntityExistsException, MyConstraintViolationException {

        // Check if area already exists
        var existingArea = em.createQuery(
                        "SELECT sa FROM ScientificArea sa WHERE sa.name = :name",
                        ScientificArea.class)
                .setParameter("name", name)
                .getResultList();

        if (!existingArea.isEmpty()) {
            throw new MyEntityExistsException("Scientific area '" + name + "' already exists");
        }

        try {
            var area = new ScientificArea(name);
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
            throw new MyEntityNotFoundException("Scientific area with id " + id + " not found");
        }
        return area;
    }

    public List<ScientificArea> findAll() {
        return em.createNamedQuery("getAllScientificAreas", ScientificArea.class)
                .getResultList();
    }

    public void update(Long id, String name)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var area = find(id);

        try {
            em.lock(area, jakarta.persistence.LockModeType.OPTIMISTIC);
            area.setName(name);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    public void delete(Long id) throws MyEntityNotFoundException {
        var area = find(id);
        em.remove(area);
    }
}
