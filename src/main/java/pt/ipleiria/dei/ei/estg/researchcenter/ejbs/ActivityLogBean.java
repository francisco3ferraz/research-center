package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.ActivityLog;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;

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
}