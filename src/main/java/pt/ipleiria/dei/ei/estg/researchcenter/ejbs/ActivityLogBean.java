package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.ActivityLog;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class ActivityLogBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public ActivityLog create(User user, String action, String entityType, Long entityId, String description) {
        var log = new ActivityLog(user, action, entityType, entityId, description);
        em.persist(log);
        return log;
    }
    
    public ActivityLog createWithChangedFields(User user, String action, String entityType, 
                                              Long entityId, String description, String changedFields) {
        var log = new ActivityLog(user, action, entityType, entityId, description);
        log.setChangedFields(changedFields);
        em.persist(log);
        return log;
    }
    
    public List<ActivityLog> getUserActivityLog(Long userId) {
        return em.createNamedQuery("getUserActivityLog", ActivityLog.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
    
    public List<ActivityLog> getPublicationHistory(Long publicationId) {
        return em.createNamedQuery("getPublicationHistory", ActivityLog.class)
                 .setParameter("publicationId", publicationId)
                 .getResultList();
    }
    
    public List<ActivityLog> getUserActivityLogPaginated(Long userId, int page, int size) {
        return em.createNamedQuery("getUserActivityLog", ActivityLog.class)
                 .setParameter("userId", userId)
                 .setFirstResult(page * size)
                 .setMaxResults(size)
                 .getResultList();
    }

    public List<ActivityLog> findByFilters(List<Long> userIds, LocalDateTime dateFrom, LocalDateTime dateTo) {
        StringBuilder sb = new StringBuilder("SELECT a FROM ActivityLog a WHERE 1=1 ");
        if (userIds != null && !userIds.isEmpty()) sb.append(" AND a.user.id IN :userIds ");
        if (dateFrom != null) sb.append(" AND a.timestamp >= :dateFrom ");
        if (dateTo != null) sb.append(" AND a.timestamp <= :dateTo ");
        sb.append(" ORDER BY a.timestamp DESC");

        var q = em.createQuery(sb.toString(), ActivityLog.class);
        if (userIds != null && !userIds.isEmpty()) q.setParameter("userIds", userIds);
        if (dateFrom != null) q.setParameter("dateFrom", dateFrom);
        if (dateTo != null) q.setParameter("dateTo", dateTo);
        return q.getResultList();
    }
}