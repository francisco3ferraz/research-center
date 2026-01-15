package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;

import java.util.List;

@Stateless
public class UserBean {
    
    @PersistenceContext
    private EntityManager em;
    
    public User find(Long id) {
        return em.find(User.class, id);
    }
    
    public User findByUsername(String username) {
        var users = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                      .setParameter("username", username)
                      .getResultList();
        
        return users.isEmpty() ? null : users.get(0);
    }
    
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.name", User.class)
                 .getResultList();
    }
    
    public boolean canLogin(String username, String password) {
        var user = findByUsername(username);
        // TODO: In production, use Hasher.verify(password, user.getPassword())
        return user != null && user.getPassword().equals(password) && user.isActive();
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        var user = findByUsername(username);
        if (user == null) return false;
        if (!user.getPassword().equals(oldPassword)) return false;
        user.setPassword(newPassword);
        em.merge(user);
        return true;
    }

    public boolean userHasPermission(String username, String permission) {
        var user = findByUsername(username);
        if (user == null) return false;
        // Simple permission model: administrators have all permissions
        return user.getRole() != null && user.getRole().name().equals("ADMINISTRADOR");
    }
}