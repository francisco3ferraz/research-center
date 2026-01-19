package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Rating;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class RatingBean {
    
    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private CollaboratorBean collaboratorBean;
    
    @EJB
    private PublicationBean publicationBean;
    
    public Rating create(int stars, Long userId, Long publicationId)
            throws MyEntityNotFoundException, MyEntityExistsException, MyConstraintViolationException {
        
        var user = collaboratorBean.find(userId);
        var publication = publicationBean.find(publicationId);
        
        // Check if user already rated this publication
        var existingRating = em.createQuery(
            "SELECT r FROM Rating r WHERE r.user = :user AND r.publication = :publication",
            Rating.class)
            .setParameter("user", user)
            .setParameter("publication", publication)
            .getResultList();
        
        if (!existingRating.isEmpty()) {
            throw new MyEntityExistsException("User " + userId + " already rated publication " + publicationId);
        }
        
        try {
            var rating = new Rating(stars, user, publication);
            em.persist(rating);
            em.flush();
            
            user.addRating(rating);
            publication.addRating(rating);
            
            return rating;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    /**
     * EP17 - Add or update user's rating for a publication.
     * If a rating exists, it is updated; otherwise, it is created.
     */
    public Rating upsert(int stars, Long userId, Long publicationId)
            throws MyEntityNotFoundException, MyConstraintViolationException {

        var user = collaboratorBean.find(userId);
        var publication = publicationBean.find(publicationId);

        var existing = em.createQuery(
                        "SELECT r FROM Rating r WHERE r.user = :user AND r.publication = :publication",
                        Rating.class)
                .setParameter("user", user)
                .setParameter("publication", publication)
                .getResultList();

        try {
            if (!existing.isEmpty()) {
                var r = existing.get(0);
                em.lock(r, jakarta.persistence.LockModeType.OPTIMISTIC);
                r.setStars(stars);
                em.flush();
                return r;
            }

            var rating = new Rating(stars, user, publication);
            em.persist(rating);
            em.flush();
            user.addRating(rating);
            publication.addRating(rating);
            return rating;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public Rating find(Long id) throws MyEntityNotFoundException {
        var rating = em.find(Rating.class, id);
        if (rating == null) {
            throw new MyEntityNotFoundException("Rating with id " + id + " not found");
        }
        return rating;
    }
    
    public Rating findByUserAndPublication(Long userId, Long publicationId) 
            throws MyEntityNotFoundException {
        var user = collaboratorBean.find(userId);
        var publication = publicationBean.find(publicationId);
        
        var ratings = em.createQuery(
            "SELECT r FROM Rating r WHERE r.user = :user AND r.publication = :publication",
            Rating.class)
            .setParameter("user", user)
            .setParameter("publication", publication)
            .getResultList();
        
        if (ratings.isEmpty()) {
            throw new MyEntityNotFoundException("Rating not found for user " + userId + " and publication " + publicationId);
        }
        return ratings.get(0);
    }
    
    public List<Rating> findByPublication(Long publicationId) throws MyEntityNotFoundException {
        var publication = publicationBean.find(publicationId);
        return em.createQuery(
            "SELECT r FROM Rating r WHERE r.publication = :publication",
            Rating.class)
            .setParameter("publication", publication)
            .getResultList();
    }
    
    public void update(Long id, int stars)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var rating = find(id);
        
        try {
            em.lock(rating, jakarta.persistence.LockModeType.OPTIMISTIC);
            rating.setStars(stars);
            em.flush();
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }
    
    public void updateByUserAndPublication(Long userId, Long publicationId, int stars)
            throws MyEntityNotFoundException, MyConstraintViolationException {
        var rating = findByUserAndPublication(userId, publicationId);
        update(rating.getId(), stars);
    }
    
    public void delete(Long id) throws MyEntityNotFoundException {
        var rating = find(id);
        em.remove(rating);
    }
    
    public void deleteByUserAndPublication(Long userId, Long publicationId) 
            throws MyEntityNotFoundException {
        var rating = findByUserAndPublication(userId, publicationId);
        delete(rating.getId());
    }
}