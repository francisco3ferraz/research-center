package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class PublicationBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private CollaboratorBean collaboratorBean;

    @EJB
    private ScientificAreaBean scientificAreaBean;

    @EJB
    private TagBean tagBean;

    public Publication create(String title, String summary, String submitterUsername, Long areaId)
            throws MyEntityNotFoundException, MyConstraintViolationException {

        var submitter = collaboratorBean.find(submitterUsername);
        var area = scientificAreaBean.find(areaId);

        try {
            var publication = new Publication(title, summary, submitter, area);
            em.persist(publication);
            em.flush();

            submitter.addPublication(publication);
            area.addPublication(publication);

            return publication;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    public Publication find(Long id) throws MyEntityNotFoundException {
        var publication = em.find(Publication.class, id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication with id " + id + " not found");
        }
        return publication;
    }

    public Publication findWithDetails(Long id) throws MyEntityNotFoundException {
        var publication = find(id);
        // Force loading of lazy collections
        Hibernate.initialize(publication.getTags());
        Hibernate.initialize(publication.getComments());
        Hibernate.initialize(publication.getRatings());
        return publication;
    }

    public List<Publication> findAll() {
        return em.createNamedQuery("getAllPublications", Publication.class)
                .getResultList();
    }

    public List<Publication> findAllIncludingHidden() {
        return em.createNamedQuery("getAllPublicationsIncludingHidden", Publication.class)
                .getResultList();
    }

    public List<Publication> findBySubmitter(String username) throws MyEntityNotFoundException {
        var submitter = collaboratorBean.find(username);
        return em.createQuery(
                        "SELECT p FROM Publication p WHERE p.submitter = :submitter ORDER BY p.uploadDate DESC",
                        Publication.class)
                .setParameter("submitter", submitter)
                .getResultList();
    }

    public List<Publication> findByArea(Long areaId) throws MyEntityNotFoundException {
        var area = scientificAreaBean.find(areaId);
        return em.createQuery(
                        "SELECT p FROM Publication p WHERE p.area = :area AND p.visible = true ORDER BY p.uploadDate DESC",
                        Publication.class)
                .setParameter("area", area)
                .getResultList();
    }

    public List<Publication> findByTag(Long tagId) throws MyEntityNotFoundException {
        var tag = tagBean.find(tagId);
        return em.createQuery(
                        "SELECT p FROM Publication p JOIN p.tags t WHERE t = :tag AND p.visible = true ORDER BY p.uploadDate DESC",
                        Publication.class)
                .setParameter("tag", tag)
                .getResultList();
    }

    public void update(Long id, String title, String summary)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var publication = find(id);

        try {
            em.lock(publication, jakarta.persistence.LockModeType.OPTIMISTIC);
            publication.setTitle(title);
            publication.setSummary(summary);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    public void delete(Long id) throws MyEntityNotFoundException {
        var publication = find(id);
        em.remove(publication);
    }

    public void addTag(Long publicationId, Long tagId) throws MyEntityNotFoundException {
        var publication = find(publicationId);
        var tag = tagBean.find(tagId);

        publication.addTag(tag);
        tag.addPublication(publication);
    }

    public void removeTag(Long publicationId, Long tagId) throws MyEntityNotFoundException {
        var publication = find(publicationId);
        var tag = tagBean.find(tagId);

        publication.removeTag(tag);
        tag.removePublication(publication);
    }

    public void hide(Long id) throws MyEntityNotFoundException {
        var publication = find(id);
        publication.setVisible(false);
    }

    public void show(Long id) throws MyEntityNotFoundException {
        var publication = find(id);
        publication.setVisible(true);
    }
}