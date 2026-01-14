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

import java.util.List;

@Stateless
public class CollaboratorBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private TagBean tagBean;

    public Collaborator create(String username, String password, String name, String email)
            throws MyEntityExistsException, MyConstraintViolationException {

        if (em.find(Collaborator.class, username) != null) {
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

    public Collaborator find(String username) throws MyEntityNotFoundException {
        var collaborator = em.find(Collaborator.class, username);
        if (collaborator == null) {
            throw new MyEntityNotFoundException("Collaborator '" + username + "' not found");
        }
        return collaborator;
    }

    public List<Collaborator> findAll() {
        return em.createQuery("SELECT c FROM Collaborator c ORDER BY c.name", Collaborator.class)
                .getResultList();
    }

    public void update(String username, String password, String name, String email)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var collaborator = find(username);

        try {
            em.lock(collaborator, jakarta.persistence.LockModeType.OPTIMISTIC);
            collaborator.setPassword(password);
            collaborator.setName(name);
            collaborator.setEmail(email);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    public void delete(String username) throws MyEntityNotFoundException {
        var collaborator = find(username);
        em.remove(collaborator);
    }

    public void subscribeToTag(String username, Long tagId)
            throws MyEntityNotFoundException {
        var collaborator = find(username);
        var tag = tagBean.find(tagId);

        collaborator.addSubscribedTag(tag);
        tag.addSubscriber(collaborator);
    }

    public void unsubscribeFromTag(String username, Long tagId)
            throws MyEntityNotFoundException {
        var collaborator = find(username);
        var tag = tagBean.find(tagId);

        collaborator.removeSubscribedTag(tag);
        tag.removeSubscriber(collaborator);
    }
}
