package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.*;
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
    
    @EJB
    private NotificationBean notificationBean;

    @EJB
    private UserBean userBean;
    
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

    public void incrementViews(Long id) throws MyEntityNotFoundException {
        var publication = find(id);
        publication.incrementViews();
        em.flush();
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
        var list = em.createQuery(
            "SELECT p FROM Publication p WHERE p.uploadedBy = :uploader ORDER BY p.uploadedAt DESC", 
            Publication.class)
            .setParameter("uploader", uploader)
            .getResultList();
        // Initialize lazy collections to avoid LazyInitializationException
        for (Publication p : list) {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getAuthors());
        }
        return list;
    }
    
    public List<Publication> findByAreaScientific(String areaScientific) {
        var list = em.createQuery(
            "SELECT p FROM Publication p WHERE p.areaScientific = :area AND p.visible = true ORDER BY p.uploadedAt DESC", 
            Publication.class)
            .setParameter("area", areaScientific)
            .getResultList();
        // Initialize lazy collections to avoid LazyInitializationException
        for (Publication p : list) {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getAuthors());
        }
        return list;
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

    public List<Publication> findWithFiltersSorted(String search, String areaScientific, Long tagId,
                                                   java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo,
                                                   String sortBy, String order,
                                                   int page, int size, boolean includeConfidential, boolean includeHidden) {
        String sort = sortBy != null ? sortBy.toLowerCase() : "date";
        String ord = order != null ? order.toLowerCase() : "desc";
        boolean asc = "asc".equals(ord);

        StringBuilder sb = new StringBuilder("SELECT p FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        if (!includeHidden) {
            sb.append(" AND p.visible = true ");
        }
        if (!includeConfidential) {
            sb.append(" AND p.confidential = false ");
        }
        if (areaScientific != null && !areaScientific.isBlank()) sb.append(" AND p.areaScientific = :area ");
        if (search != null && !search.isBlank()) sb.append(" AND (LOWER(p.title) LIKE :search OR EXISTS (SELECT a FROM p.authors a WHERE LOWER(a) LIKE :search)) ");
        if (tagId != null) sb.append(" AND t.id = :tagId ");
        if (dateFrom != null) sb.append(" AND p.uploadedAt >= :dateFrom ");
        if (dateTo != null) sb.append(" AND p.uploadedAt <= :dateTo ");

        // Sorting: date, rating, ratings_count, comments
        String orderClause;
        switch (sort) {
            case "rating":
                // Average rating via correlated subquery
                orderClause = " ORDER BY (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) ";
                break;
            case "ratings_count":
                orderClause = " ORDER BY (SELECT COUNT(r2) FROM Rating r2 WHERE r2.publication = p) ";
                break;
            case "comments":
                orderClause = " ORDER BY (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) ";
                break;
            case "views":
                orderClause = " ORDER BY p.viewsCount ";
                break;
            case "date":
            default:
                orderClause = " ORDER BY p.uploadedAt ";
                break;
        }
        orderClause += asc ? "ASC" : "DESC";
        sb.append(orderClause);

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
        for (Publication p : list) {
            try {
                Hibernate.initialize(p.getTags());
                Hibernate.initialize(p.getComments());
                Hibernate.initialize(p.getRatings());
                Hibernate.initialize(p.getAuthors());
            } catch (Exception ignore) {}
        }
        return list;
    }

    public List<Publication> findPublicWithFiltersSorted(String search, String areaScientific, Long tagId,
                                                         java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo,
                                                         String sortBy, String order,
                                                         int page, int size) {
        // Same as findWithFiltersSorted but excludes confidential publications (for guest access)
        String sort = sortBy != null ? sortBy.toLowerCase() : "date";
        String ord = order != null ? order.toLowerCase() : "desc";
        boolean asc = "asc".equals(ord);

        StringBuilder sb = new StringBuilder("SELECT p FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        sb.append(" AND p.visible = true ");
        sb.append(" AND p.confidential = false ");
        if (areaScientific != null && !areaScientific.isBlank()) sb.append(" AND p.areaScientific = :area ");
        if (search != null && !search.isBlank()) sb.append(" AND (LOWER(p.title) LIKE :search OR EXISTS (SELECT a FROM p.authors a WHERE LOWER(a) LIKE :search)) ");
        if (tagId != null) sb.append(" AND t.id = :tagId ");
        if (dateFrom != null) sb.append(" AND p.uploadedAt >= :dateFrom ");
        if (dateTo != null) sb.append(" AND p.uploadedAt <= :dateTo ");

        String orderClause;
        switch (sort) {
            case "rating":
                orderClause = " ORDER BY (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) ";
                break;
            case "ratings_count":
                orderClause = " ORDER BY (SELECT COUNT(r2) FROM Rating r2 WHERE r2.publication = p) ";
                break;
            case "comments":
                orderClause = " ORDER BY (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) ";
                break;
            case "views":
                orderClause = " ORDER BY p.viewsCount ";
                break;
            case "date":
            default:
                orderClause = " ORDER BY p.uploadedAt ";
                break;
        }
        orderClause += asc ? "ASC" : "DESC";
        sb.append(orderClause);

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
        for (Publication p : list) {
            try {
                Hibernate.initialize(p.getTags());
                Hibernate.initialize(p.getComments());
                Hibernate.initialize(p.getRatings());
                Hibernate.initialize(p.getAuthors());
            } catch (Exception ignore) {}
        }
        return list;
    }

    public long countPublicWithFilters(String search, String areaScientific, Long tagId,
                                       java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        sb.append(" AND p.visible = true ");
        sb.append(" AND p.confidential = false ");
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

    public List<Publication> advancedSearch(List<String> keywords,
                                           List<String> authors,
                                           List<PublicationType> types,
                                           List<Long> scientificAreaIds,
                                           List<Long> tagIds,
                                           Integer yearFrom,
                                           Integer yearTo,
                                           Double minRating,
                                           Boolean hasComments,
                                           Boolean confidential,
                                           String sortBy,
                                           String order,
                                           int page,
                                           int size) {
        String sort = sortBy != null ? sortBy.toLowerCase() : "date";
        String ord = order != null ? order.toLowerCase() : "desc";
        boolean asc = "asc".equals(ord);

        StringBuilder sb = new StringBuilder("SELECT DISTINCT p FROM Publication p ");
        if (tagIds != null && !tagIds.isEmpty()) sb.append(" JOIN p.tags t ");
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) sb.append(" JOIN p.area a ");
        sb.append(" WHERE 1=1 ");
        sb.append(" AND p.visible = true ");

        if (confidential != null) sb.append(" AND p.confidential = :confidential ");
        if (yearFrom != null) sb.append(" AND p.year >= :yearFrom ");
        if (yearTo != null) sb.append(" AND p.year <= :yearTo ");
        if (types != null && !types.isEmpty()) sb.append(" AND p.type IN :types ");
        if (tagIds != null && !tagIds.isEmpty()) sb.append(" AND t.id IN :tagIds ");
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) sb.append(" AND a.id IN :areaIds ");

        if (authors != null && !authors.isEmpty()) {
            sb.append(" AND (");
            for (int i = 0; i < authors.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(" EXISTS (SELECT au FROM p.authors au WHERE LOWER(au) LIKE :author").append(i).append(") ");
            }
            sb.append(") ");
        }

        if (keywords != null && !keywords.isEmpty()) {
            sb.append(" AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(" LOWER(p.title) LIKE :kw").append(i)
                  .append(" OR LOWER(p.abstract_) LIKE :kw").append(i)
                  .append(" OR EXISTS (SELECT au2 FROM p.authors au2 WHERE LOWER(au2) LIKE :kw").append(i).append(") ");
            }
            sb.append(") ");
        }

        if (minRating != null) {
            sb.append(" AND (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) >= :minRating ");
        }

        if (hasComments != null) {
            if (hasComments) {
                sb.append(" AND (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) > 0 ");
            } else {
                sb.append(" AND (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) = 0 ");
            }
        }

        String orderClause;
        switch (sort) {
            case "rating":
                orderClause = " ORDER BY (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) ";
                break;
            case "ratings_count":
                orderClause = " ORDER BY (SELECT COUNT(r2) FROM Rating r2 WHERE r2.publication = p) ";
                break;
            case "comments":
                orderClause = " ORDER BY (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) ";
                break;
            case "views":
                orderClause = " ORDER BY p.viewsCount ";
                break;
            case "date":
            default:
                orderClause = " ORDER BY p.uploadedAt ";
                break;
        }
        orderClause += asc ? "ASC" : "DESC";
        sb.append(orderClause);

        var q = em.createQuery(sb.toString(), Publication.class);
        if (confidential != null) q.setParameter("confidential", confidential);
        if (yearFrom != null) q.setParameter("yearFrom", yearFrom);
        if (yearTo != null) q.setParameter("yearTo", yearTo);
        if (types != null && !types.isEmpty()) q.setParameter("types", types);
        if (tagIds != null && !tagIds.isEmpty()) q.setParameter("tagIds", tagIds);
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) q.setParameter("areaIds", scientificAreaIds);
        if (minRating != null) q.setParameter("minRating", minRating);

        if (authors != null) {
            for (int i = 0; i < authors.size(); i++) {
                q.setParameter("author" + i, "%" + authors.get(i).toLowerCase() + "%");
            }
        }
        if (keywords != null) {
            for (int i = 0; i < keywords.size(); i++) {
                q.setParameter("kw" + i, "%" + keywords.get(i).toLowerCase() + "%");
            }
        }

        if (page >= 0 && size > 0) {
            q.setFirstResult(page * size);
            q.setMaxResults(size);
        }

        var list = q.getResultList();
        for (Publication p : list) {
            try {
                Hibernate.initialize(p.getTags());
                Hibernate.initialize(p.getComments());
                Hibernate.initialize(p.getRatings());
                Hibernate.initialize(p.getAuthors());
            } catch (Exception ignore) {}
        }
        return list;
    }

    public long countAdvancedSearch(List<String> keywords,
                                   List<String> authors,
                                   List<PublicationType> types,
                                   List<Long> scientificAreaIds,
                                   List<Long> tagIds,
                                   Integer yearFrom,
                                   Integer yearTo,
                                   Double minRating,
                                   Boolean hasComments,
                                   Boolean confidential) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Publication p ");
        if (tagIds != null && !tagIds.isEmpty()) sb.append(" JOIN p.tags t ");
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) sb.append(" JOIN p.area a ");
        sb.append(" WHERE 1=1 ");
        sb.append(" AND p.visible = true ");

        if (confidential != null) sb.append(" AND p.confidential = :confidential ");
        if (yearFrom != null) sb.append(" AND p.year >= :yearFrom ");
        if (yearTo != null) sb.append(" AND p.year <= :yearTo ");
        if (types != null && !types.isEmpty()) sb.append(" AND p.type IN :types ");
        if (tagIds != null && !tagIds.isEmpty()) sb.append(" AND t.id IN :tagIds ");
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) sb.append(" AND a.id IN :areaIds ");

        if (authors != null && !authors.isEmpty()) {
            sb.append(" AND (");
            for (int i = 0; i < authors.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(" EXISTS (SELECT au FROM p.authors au WHERE LOWER(au) LIKE :author").append(i).append(") ");
            }
            sb.append(") ");
        }

        if (keywords != null && !keywords.isEmpty()) {
            sb.append(" AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(" LOWER(p.title) LIKE :kw").append(i)
                  .append(" OR LOWER(p.abstract_) LIKE :kw").append(i)
                  .append(" OR EXISTS (SELECT au2 FROM p.authors au2 WHERE LOWER(au2) LIKE :kw").append(i).append(") ");
            }
            sb.append(") ");
        }

        if (minRating != null) {
            sb.append(" AND (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) >= :minRating ");
        }

        if (hasComments != null) {
            if (hasComments) sb.append(" AND (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) > 0 ");
            else sb.append(" AND (SELECT COUNT(c) FROM Comment c WHERE c.publication = p AND c.visible = true) = 0 ");
        }

        var q = em.createQuery(sb.toString(), Long.class);
        if (confidential != null) q.setParameter("confidential", confidential);
        if (yearFrom != null) q.setParameter("yearFrom", yearFrom);
        if (yearTo != null) q.setParameter("yearTo", yearTo);
        if (types != null && !types.isEmpty()) q.setParameter("types", types);
        if (tagIds != null && !tagIds.isEmpty()) q.setParameter("tagIds", tagIds);
        if (scientificAreaIds != null && !scientificAreaIds.isEmpty()) q.setParameter("areaIds", scientificAreaIds);
        if (minRating != null) q.setParameter("minRating", minRating);

        if (authors != null) {
            for (int i = 0; i < authors.size(); i++) {
                q.setParameter("author" + i, "%" + authors.get(i).toLowerCase() + "%");
            }
        }
        if (keywords != null) {
            for (int i = 0; i < keywords.size(); i++) {
                q.setParameter("kw" + i, "%" + keywords.get(i).toLowerCase() + "%");
            }
        }

        return q.getSingleResult();
    }

    public List<Publication> findAllHidden() {
        var list = em.createQuery("SELECT p FROM Publication p WHERE p.visible = false ORDER BY p.uploadedAt DESC", Publication.class)
                .getResultList();
        for (Publication p : list) {
            try {
                Hibernate.initialize(p.getTags());
                Hibernate.initialize(p.getComments());
                Hibernate.initialize(p.getRatings());
                Hibernate.initialize(p.getAuthors());
            } catch (Exception ignore) {}
        }
        return list;
    }

    public long countWithFilters(String search, String areaScientific, Long tagId,
                                  java.time.LocalDateTime dateFrom, java.time.LocalDateTime dateTo, boolean includeConfidential, boolean includeHidden) {
        StringBuilder sb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Publication p ");
        if (tagId != null) sb.append(" JOIN p.tags t ");
        sb.append(" WHERE 1=1 ");
        if (!includeHidden) {
            sb.append(" AND p.visible = true ");
        }
        if (!includeConfidential) {
            sb.append(" AND p.confidential = false ");
        }
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
        var list = em.createQuery(
            "SELECT p FROM Publication p JOIN p.tags t WHERE t = :tag AND p.visible = true ORDER BY p.uploadedAt DESC",
            Publication.class)
            .setParameter("tag", tag)
            .getResultList();
        // Initialize lazy collections to avoid LazyInitializationException
        for (Publication p : list) {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getAuthors());
        }
        return list;
    }
    
    public List<Publication> findByType(PublicationType type) {
        var list = em.createQuery(
            "SELECT p FROM Publication p WHERE p.type = :type AND p.visible = true ORDER BY p.uploadedAt DESC",
            Publication.class)
            .setParameter("type", type)
            .getResultList();
        // Initialize lazy collections to avoid LazyInitializationException
        for (Publication p : list) {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getAuthors());
        }
        return list;
    }
    
    public void update(Long id, String title, List<String> authors, String abstract_, 
                      String aiGeneratedSummary, Integer year, String publisher, String doi,
                      boolean visible, boolean confidential)
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
            publication.setVisible(visible);
            publication.setConfidential(confidential);
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
        
        // Notify subscribers of this tag about the new publication
        String authorName = publication.getUploadedBy() != null 
            ? publication.getUploadedBy().getName() 
            : "Utilizador desconhecido";
        notificationBean.notifyTagSubscribers(
            tagId,
            "NEW_PUBLICATION_WITH_TAG",
            "Nova publicação na tag " + tag.getName(),
            authorName + " adicionou uma nova publicação: '" + publication.getTitle() + "'",
            "PUBLICATION",
            publicationId
        );
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

    public void setVisibility(Long id, boolean visible, String performedByUsername) throws MyEntityNotFoundException {
        var publication = find(id);
        publication.setVisible(visible);
        publication.setUpdatedAt(LocalDateTime.now());
        if (!visible) {
            var u = performedByUsername != null ? userBean.findByUsername(performedByUsername) : null;
            publication.setHiddenBy(u);
            publication.setHiddenAt(LocalDateTime.now());
        } else {
            publication.setHiddenBy(null);
            publication.setHiddenAt(null);
        }
    }
    
    public void hide(Long id) throws MyEntityNotFoundException {
        setVisibility(id, false);
    }
    
    public void show(Long id) throws MyEntityNotFoundException {
        setVisibility(id, true);
    }
}