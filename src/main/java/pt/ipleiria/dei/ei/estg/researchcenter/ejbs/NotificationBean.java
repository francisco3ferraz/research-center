package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Notification;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class NotificationBean {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private CollaboratorBean collaboratorBean;
    
    public Notification create(Long userId, String type, String title, String message,
                              String relatedEntityType, Long relatedEntityId) 
            throws MyEntityNotFoundException {
        var user = collaboratorBean.find(userId);
        var notification = new Notification(user, type, title, message, relatedEntityType, relatedEntityId);
        em.persist(notification);
        return notification;
    }
    
    public Notification find(Long id) throws MyEntityNotFoundException {
        var notification = em.find(Notification.class, id);
        if (notification == null) {
            throw new MyEntityNotFoundException("Notification with id " + id + " not found");
        }
        return notification;
    }
    
    public List<Notification> getUserNotifications(Long userId) {
        return em.createNamedQuery("getUserNotifications", Notification.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return em.createNamedQuery("getUnreadNotifications", Notification.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }
    
    public long countUnreadNotifications(Long userId) {
        return em.createNamedQuery("countUnreadNotifications", Long.class)
                 .setParameter("userId", userId)
                 .getSingleResult();
    }
    
    public List<Notification> getUserNotificationsPaginated(Long userId, boolean unreadOnly, int page, int size) {
        var query = unreadOnly ? "getUnreadNotifications" : "getUserNotifications";
        return em.createNamedQuery(query, Notification.class)
                 .setParameter("userId", userId)
                 .setFirstResult(page * size)
                 .setMaxResults(size)
                 .getResultList();
    }
    
    public void markAsRead(Long id) throws MyEntityNotFoundException {
        var notification = find(id);
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
    }
    
    public int markAllAsRead(Long userId) {
        var notifications = getUnreadNotifications(userId);
        for (var notification : notifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return notifications.size();
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var notification = find(id);
        em.remove(notification);
    }
    
    // Helper method to notify subscribers of a tag
    public void notifyTagSubscribers(Long tagId, String type, String title, String message,
                                     String relatedEntityType, Long relatedEntityId) {
        var subscribers = em.createQuery(
            "SELECT c FROM Collaborator c JOIN c.subscribedTags t WHERE t.id = :tagId",
            Collaborator.class)
            .setParameter("tagId", tagId)
            .getResultList();
        
        for (var subscriber : subscribers) {
            var notification = new Notification(subscriber, type, title, message, relatedEntityType, relatedEntityId);
            em.persist(notification);
        }
    }
}