package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class StatisticsBean {

    @PersistenceContext
    private EntityManager em;

    private long count(String jpql) {
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public Map<String, Object> getOverview() {
        long totalPublications = count("SELECT COUNT(p) FROM Publication p");
        long totalUsers = count("SELECT COUNT(u) FROM User u");
        long totalComments = count("SELECT COUNT(c) FROM Comment c");
        long totalRatings = count("SELECT COUNT(r) FROM Rating r");
        long totalTags = count("SELECT COUNT(t) FROM Tag t");

        // publicationsByType
        Map<String, Long> byType = new HashMap<>();
        for (PublicationType t : PublicationType.values()) {
            Long c = em.createQuery("SELECT COUNT(p) FROM Publication p WHERE p.type = :t", Long.class)
                    .setParameter("t", t)
                    .getSingleResult();
            byType.put(t.name(), c);
        }

        // publicationsByArea (string-based areaScientific)
        Map<String, Long> byArea = new HashMap<>();
        List<Object[]> rows = em.createQuery("SELECT p.areaScientific, COUNT(p) FROM Publication p GROUP BY p.areaScientific", Object[].class)
                .getResultList();
        for (Object[] r : rows) {
            byArea.put(String.valueOf(r[0]), (Long) r[1]);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime d7 = now.minusDays(7);
        LocalDateTime d30 = now.minusDays(30);

        long newPublications7 = em.createQuery("SELECT COUNT(p) FROM Publication p WHERE p.uploadedAt >= :d", Long.class)
                .setParameter("d", d7)
                .getSingleResult();
        long newComments7 = em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :d", Long.class)
                .setParameter("d", d7)
                .getSingleResult();
        long newRatings7 = em.createQuery("SELECT COUNT(r) FROM Rating r WHERE r.createdAt >= :d", Long.class)
                .setParameter("d", d7)
                .getSingleResult();

        long newPublications30 = em.createQuery("SELECT COUNT(p) FROM Publication p WHERE p.uploadedAt >= :d", Long.class)
                .setParameter("d", d30)
                .getSingleResult();
        long newComments30 = em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.createdAt >= :d", Long.class)
                .setParameter("d", d30)
                .getSingleResult();
        long newRatings30 = em.createQuery("SELECT COUNT(r) FROM Rating r WHERE r.createdAt >= :d", Long.class)
                .setParameter("d", d30)
                .getSingleResult();

        Map<String, Object> recentActivity = Map.of(
                "last7Days", Map.of(
                        "newPublications", newPublications7,
                        "newComments", newComments7,
                        "newRatings", newRatings7
                ),
                "last30Days", Map.of(
                        "newPublications", newPublications30,
                        "newComments", newComments30,
                        "newRatings", newRatings30
                )
        );

        return Map.of(
                "totalPublications", totalPublications,
                "totalUsers", totalUsers,
                "totalComments", totalComments,
                "totalRatings", totalRatings,
                "totalTags", totalTags,
                "publicationsByType", byType,
                "publicationsByArea", byArea,
                "recentActivity", recentActivity
        );
    }

    public Map<String, Object> getPersonal(String username) {
        User user = em.createQuery("SELECT u FROM User u WHERE u.username = :u", User.class)
                .setParameter("u", username)
                .getResultStream().findFirst().orElse(null);
        if (user == null) {
            return Map.of("userId", -1);
        }

        Long userId = user.getId();

        long publicationsUploaded = em.createQuery("SELECT COUNT(p) FROM Publication p WHERE p.uploadedBy.username = :u", Long.class)
                .setParameter("u", username)
                .getSingleResult();
        long commentsCreated = em.createQuery("SELECT COUNT(c) FROM Comment c WHERE c.author.username = :u", Long.class)
                .setParameter("u", username)
                .getSingleResult();
        long ratingsGiven = em.createQuery("SELECT COUNT(r) FROM Rating r WHERE r.user.username = :u", Long.class)
                .setParameter("u", username)
                .getSingleResult();

        // Average rating received for user's publications
        Double avgRatingReceived = em.createQuery(
                "SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication.uploadedBy.username = :u",
                Double.class
        ).setParameter("u", username).getSingleResult();
        Long totalRatingsReceived = em.createQuery(
                "SELECT COUNT(r) FROM Rating r WHERE r.publication.uploadedBy.username = :u",
                Long.class
        ).setParameter("u", username).getSingleResult();

        // Most active tag: by count of publications by this user having that tag
        Object[] mostActive = em.createQuery(
                "SELECT t.id, t.name, COUNT(p) " +
                        "FROM Publication p JOIN p.tags t " +
                        "WHERE p.uploadedBy.username = :u " +
                        "GROUP BY t.id, t.name " +
                        "ORDER BY COUNT(p) DESC",
                Object[].class
        ).setParameter("u", username)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        Map<String, Object> mostActiveTag = mostActive == null
                ? null
                : Map.of("id", mostActive[0], "name", mostActive[1], "activityCount", mostActive[2]);

        // Recent uploads
        List<Object[]> recentUploads = em.createQuery(
                "SELECT p.id, p.title, p.uploadedAt FROM Publication p WHERE p.uploadedBy.username = :u ORDER BY p.uploadedAt DESC",
                Object[].class
        ).setParameter("u", username)
                .setMaxResults(5)
                .getResultList();

        var recentUploadsList = recentUploads.stream().map(r -> Map.of(
                "id", r[0],
                "title", r[1],
                "uploadedAt", r[2] != null ? r[2].toString() : null
        )).toList();

        return Map.of(
                "userId", userId,
                "publicationsUploaded", publicationsUploaded,
                "commentsCreated", commentsCreated,
                "ratingsGiven", ratingsGiven,
                "tagsSubscribed", em.createQuery("SELECT COUNT(t) FROM Collaborator c JOIN c.subscribedTags t WHERE c.username = :u", Long.class)
                        .setParameter("u", username)
                        .getResultStream().findFirst().orElse(0L),
                "averageRatingReceived", avgRatingReceived != null ? avgRatingReceived : 0.0,
                "totalRatingsReceived", totalRatingsReceived != null ? totalRatingsReceived : 0,
                "mostActiveTag", mostActiveTag,
                "recentUploads", recentUploadsList
        );
    }

    public List<Map<String, Object>> getTopPublications(String criteria, int limit) {
        int lim = Math.max(1, Math.min(limit, 100));
        String c = criteria != null ? criteria.toLowerCase() : "rating";

        List<Publication> pubs;
        switch (c) {
            case "comments":
                pubs = em.createQuery(
                                "SELECT p FROM Publication p ORDER BY (SELECT COUNT(c) FROM Comment c WHERE c.publication = p) DESC",
                                Publication.class)
                        .setMaxResults(lim)
                        .getResultList();
                break;
            case "views":
                pubs = em.createQuery("SELECT p FROM Publication p ORDER BY p.viewsCount DESC", Publication.class)
                        .setMaxResults(lim)
                        .getResultList();
                break;
            case "rating":
            default:
                pubs = em.createQuery(
                                "SELECT p FROM Publication p ORDER BY (SELECT COALESCE(AVG(r.stars),0) FROM Rating r WHERE r.publication = p) DESC",
                                Publication.class)
                        .setMaxResults(lim)
                        .getResultList();
                break;
        }

        return pubs.stream().map(p -> Map.of(
                "id", p.getId(),
                "title", p.getTitle(),
                "averageRating", p.getAverageRating(),
                "ratingsCount", p.getRatings() != null ? p.getRatings().size() : 0,
                "commentsCount", p.getComments() != null ? p.getComments().size() : 0,
                "viewsCount", p.getViewsCount(),
                "uploadedBy", p.getUploadedBy() != null ? Map.of("id", p.getUploadedBy().getId(), "name", p.getUploadedBy().getName()) : null
        )).toList();
    }
}

