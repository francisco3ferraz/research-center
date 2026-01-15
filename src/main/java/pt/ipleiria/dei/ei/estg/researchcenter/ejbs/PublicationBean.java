package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationDTO;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class PublicationBean {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private CollaboratorBean collaboratorBean;
    
    @EJB
    private TagBean tagBean;
    
    public Publication create(String title, List<String> authors, PublicationType type,
                             String areaScientific, Integer year, String abstract_,
                             Long uploadedById)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var uploadedBy = uploadedById != null ? collaboratorBean.find(uploadedById) : null;

        try {
            var publication = new Publication(title, authors, type, areaScientific, year, abstract_, uploadedBy);
            em.persist(publication);
            em.flush();

            if (uploadedBy != null) {
                uploadedBy.addPublication(publication);
            }

            return publication;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    public PublicationDTO getDTOWithDetails(Long id) throws MyEntityNotFoundException {
        var publication = findWithDetails(id);
        return PublicationDTO.fromWithDetails(publication);
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
        Hibernate.initialize(publication.getAuthors());
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
    
    public List<Publication> findByUploadedBy(Long userId) throws MyEntityNotFoundException {
        var uploader = collaboratorBean.find(userId);
        return em.createQuery(
            "SELECT p FROM Publication p WHERE p.uploadedBy = :uploader ORDER BY p.uploadedAt DESC", 
            Publication.class)
            .setParameter("uploader", uploader)
            .getResultList();
    }
    
    public List<Publication> findByAreaScientific(String areaScientific) {
        return em.createQuery(
            "SELECT p FROM Publication p WHERE p.areaScientific = :area AND p.visible = true ORDER BY p.uploadedAt DESC", 
            Publication.class)
            .setParameter("area", areaScientific)
            .getResultList();
    }

    public List<Publication> findWithFilters(String search, String areaScientific, Long tagId,
                                             java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo,
                                             int page, int size) {
        StringBuilder sb = new StringBuilder("SELECT p FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        if (areaScientific != null && !areaScientific.isBlank()) sb.append(" AND p.areaScientific = :area ");
        if (search != null && !search.isBlank()) sb.append(" AND (LOWER(p.title) LIKE :search OR EXISTS (SELECT a FROM p.authors a WHERE LOWER(a) LIKE :search)) ");
        if (tagId != null) sb.append(" AND t.id = :tagId ");
        if (dateFrom != null) sb.append(" AND p.uploadedAt >= :dateFrom ");
        if (dateTo != null) sb.append(" AND p.uploadedAt <= :dateTo ");
        sb.append(" ORDER BY p.uploadedAt DESC");

        var q = em.createQuery(sb.toString(), Publication.class);
        if (areaScientific != null && !areaScientific.isBlank()) q.setParameter("area", areaScientific);
        if (search != null && !search.isBlank()) q.setParameter("search", "%" + search.toLowerCase() + "%");
        if (tagId != null) q.setParameter("tagId", tagId);
        if (dateFrom != null) q.setParameter("dateFrom", dateFrom);
        if (dateTo != null) q.setParameter("dateTo", dateTo);
        if (page >= 0 && size > 0) {
            q.setFirstResult(page * size);
            q.setMaxResults(size);
        }
        var list = q.getResultList();
        // Ensure lazy collections are initialized to avoid lazy-init outside transaction
        for (Publication p : list) {
            try {
                Hibernate.initialize(p.getTags());
                Hibernate.initialize(p.getComments());
                Hibernate.initialize(p.getRatings());
                Hibernate.initialize(p.getAuthors());
            } catch (Exception ignore) {
            }
        }
        return list;
    }

    public long countWithFilters(String search, String areaScientific, Long tagId,
                                  java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        if (areaScientific != null && !areaScientific.isBlank()) sb.append(" AND p.areaScientific = :area ");
        if (search != null && !search.isBlank()) sb.append(" AND (LOWER(p.title) LIKE :search OR EXISTS (SELECT a FROM p.authors a WHERE LOWER(a) LIKE :search)) ");
        if (tagId != null) sb.append(" AND t.id = :tagId ");
        if (dateFrom != null) sb.append(" AND p.uploadedAt >= :dateFrom ");
        if (dateTo != null) sb.append(" AND p.uploadedAt <= :dateTo ");

        var q = em.createQuery(sb.toString(), Long.class);
        if (areaScientific != null && !areaScientific.isBlank()) q.setParameter("area", areaScientific);
        if (search != null && !search.isBlank()) q.setParameter("search", "%" + search.toLowerCase() + "%");
        if (tagId != null) q.setParameter("tagId", tagId);
        if (dateFrom != null) q.setParameter("dateFrom", dateFrom);
        if (dateTo != null) q.setParameter("dateTo", dateTo);
        return q.getSingleResult();
    }
    
    public List<Publication> findByTag(Long tagId) throws MyEntityNotFoundException {
        var tag = tagBean.find(tagId);
        return em.createQuery(
            "SELECT p FROM Publication p JOIN p.tags t WHERE t = :tag AND p.visible = true ORDER BY p.uploadedAt DESC",
            Publication.class)
            .setParameter("tag", tag)
            .getResultList();
    }
    
    public List<Publication> findByType(PublicationType type) {
        return em.createQuery(
            "SELECT p FROM Publication p WHERE p.type = :type AND p.visible = true ORDER BY p.uploadedAt DESC",
            Publication.class)
            .setParameter("type", type)
            .getResultList();
    }
    
    public void update(Long id, String title, List<String> authors, String abstract_, 
                      String aiGeneratedSummary, Integer year, String publisher, String doi)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var publication = find(id);
        
        try {
            em.lock(publication, jakarta.persistence.LockModeType.OPTIMISTIC);
            publication.setTitle(title);
            if (authors != null) {
                publication.setAuthors(authors);
            }
            publication.setAbstract_(abstract_);
            publication.setAiGeneratedSummary(aiGeneratedSummary);
            publication.setYear(year);
            publication.setPublisher(publisher);
            publication.setDoi(doi);
            publication.setUpdatedAt(LocalDateTime.now());
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
    
    public void setVisibility(Long id, boolean visible) throws MyEntityNotFoundException {
        var publication = find(id);
        publication.setVisible(visible);
        publication.setUpdatedAt(LocalDateTime.now());
    }
    
    public void hide(Long id) throws MyEntityNotFoundException {
        setVisibility(id, false);
    }
    
    public void show(Long id) throws MyEntityNotFoundException {
        setVisibility(id, true);
    }
}