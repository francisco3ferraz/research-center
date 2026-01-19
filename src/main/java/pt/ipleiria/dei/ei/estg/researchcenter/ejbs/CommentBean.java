package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Comment;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class CommentBean {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private CollaboratorBean collaboratorBean;
    
    @EJB
    private PublicationBean publicationBean;
    
    @EJB
    private NotificationBean notificationBean;

    @EJB
    private UserBean userBean;
    
    public Comment create(String text, Long authorId, Long publicationId)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        
        var author = collaboratorBean.find(authorId);
        var publication = publicationBean.find(publicationId);
        
        try {
            var comment = new Comment(text, author, publication);
            em.persist(comment);
            em.flush();
            
            author.addComment(comment);
            publication.addComment(comment);
            
            // Notify subscribers of this publication's tags about the new comment
            Hibernate.initialize(publication.getTags());
            for (Tag tag : publication.getTags()) {
                // Truncate comment text for notification message
                String commentPreview = text.length() > 50 
                    ? text.substring(0, 50) + "..." 
                    : text;
                notificationBean.notifyTagSubscribers(
                    tag.getId(),
                    "NEW_COMMENT_ON_TAG",
                    "Novo comentário na tag " + tag.getName(),
                    author.getName() + " comentou: '" + commentPreview + "'",
                    "COMMENT",
                    comment.getId()
                );
            }
            
            return comment;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public Comment find(Long id) throws MyEntityNotFoundException {
        var comment = em.find(Comment.class, id);
        if (comment == null) {
            throw new MyEntityNotFoundException("Comment with id " + id + " not found");
        }
        return comment;
    }
    
    public List<Comment> findByPublication(Long publicationId) throws MyEntityNotFoundException {
        var publication = publicationBean.find(publicationId);
        return em.createQuery(
            "SELECT c FROM Comment c WHERE c.publication = :publication AND c.visible = true ORDER BY c.createdAt DESC",
            Comment.class)
            .setParameter("publication", publication)
            .getResultList();
    }
    
    public List<Comment> findAllByPublicationIncludingHidden(Long publicationId) 
            throws MyEntityNotFoundException {
        var publication = publicationBean.find(publicationId);
        return em.createQuery(
            "SELECT c FROM Comment c WHERE c.publication = :publication ORDER BY c.createdAt DESC",
            Comment.class)
            .setParameter("publication", publication)
            .getResultList();
    }
    
    public List<Comment> findAllHidden() {
        return em.createQuery(
            "SELECT c FROM Comment c WHERE c.visible = false ORDER BY c.createdAt DESC",
            Comment.class)
            .getResultList();
    }
    
    public void update(Long id, String text)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var comment = find(id);
        
        try {
            em.lock(comment, jakarta.persistence.LockModeType.OPTIMISTIC);
            comment.setText(text);
            comment.setUpdatedAt(LocalDateTime.now());
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var comment = find(id);
        em.remove(comment);
    }
    
    public void setVisibility(Long id, boolean visible) throws MyEntityNotFoundException {
        var comment = find(id);
        comment.setVisible(visible);
        comment.setUpdatedAt(LocalDateTime.now());
    }

    public void setVisibility(Long id, boolean visible, String performedByUsername) throws MyEntityNotFoundException {
        var comment = find(id);
        comment.setVisible(visible);
        comment.setUpdatedAt(LocalDateTime.now());
        if (!visible) {
            User u = performedByUsername != null ? userBean.findByUsername(performedByUsername) : null;
            comment.setHiddenBy(u);
            comment.setHiddenAt(LocalDateTime.now());
        } else {
            comment.setHiddenBy(null);
            comment.setHiddenAt(null);
        }
    }
    
    public void hide(Long id) throws MyEntityNotFoundException {
        setVisibility(id, false);
    }
    
    public void show(Long id) throws MyEntityNotFoundException {
        setVisibility(id, true);
    }
}