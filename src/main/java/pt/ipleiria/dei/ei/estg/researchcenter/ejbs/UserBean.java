package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.*;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.*;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Hasher;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class UserBean {

    @PersistenceContext
    private EntityManager em;

    public User find(Long id) throws MyEntityNotFoundException {
        var user = em.find(User.class, id);
        if (user == null) {
            throw new MyEntityNotFoundException("User with id " + id + " not found");
        }
        return user;
    }
    
    public User findByUsername(String username) {
        var users = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                      .setParameter("username", username)
                      .getResultList();
        
        return users.isEmpty() ? null : users.get(0);
    }
    
    public User findByEmail(String email) {
        var users = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                      .setParameter("email", email)
                      .getResultList();
        
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByUsernameOrThrow(String username) throws MyEntityNotFoundException {
        var user = findByUsername(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User '" + username + "' not found");
        }
        return user;
    }

    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.name", User.class)
                 .getResultList();
    }

    /**
     * Searches users by name (case-insensitive, contains).
     * Limited to 20 results to avoid returning too many records for autocomplete.
     */
    public List<User> searchByName(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }
        var pattern = "%" + q.toLowerCase() + "%";
        return em.createQuery("SELECT u FROM User u WHERE LOWER(u.name) LIKE :pattern ORDER BY u.name", User.class)
                 .setParameter("pattern", pattern)
                 .setMaxResults(20)
                 .getResultList();
    }

    /**
     * Creates a new user based on the specified role.
     * EP03 - Criar Utilizador
     */
    public User create(String username, String password, String name, String email, UserRole role)
            throws MyEntityExistsException, MyConstraintViolationException {

        // Check if username already exists (across all user types)
        if (findByUsername(username) != null) {
            throw new MyEntityExistsException("User with username '" + username + "' already exists");
        }

        try {
            User user;
            // Create user based on role - each bean already hashes the password
            switch (role) {
                case COLABORADOR:
                    user = new Collaborator(username, Hasher.hash(password), name, email);
                    break;
                case RESPONSAVEL:
                    user = new Manager(username, Hasher.hash(password), name, email);
                    break;
                case ADMINISTRADOR:
                    user = new Administrator(username, Hasher.hash(password), name, email);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid role: " + role);
            }
            em.persist(user);
            em.flush();
            return user;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    /**
     * Updates a user's information.
     * EP04 - Editar Utilizador
     */
    public User update(Long id, String name, String email, UserRole newRole)
            throws MyEntityNotFoundException, MyConstraintViolationException {

        var user = find(id);

        try {
            em.lock(user, jakarta.persistence.LockModeType.OPTIMISTIC);
            user.setName(name);
            user.setEmail(email);

            // Note: Role change with SINGLE_TABLE inheritance just updates the role field
            // The actual class type doesn't change, but the role determines permissions
            if (newRole != null && !newRole.equals(user.getRole())) {
                user.setRole(newRole);
            }

            em.flush();
            return user;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    /**
     * Updates the authenticated user's own profile.
     * EP07 - Editar Perfil Próprio
     */
    public User updateProfile(String username, String name, String email)
            throws MyEntityNotFoundException, MyConstraintViolationException {

        var user = findByUsernameOrThrow(username);

        try {
            em.lock(user, jakarta.persistence.LockModeType.OPTIMISTIC);
            user.setName(name);
            user.setEmail(email);
            em.flush();
            return user;
        } catch (ConstraintViolationException e) {
            throw new MyConstraintViolationException(e);
        }
    }

    /**
     * Activates or deactivates a user.
     * EP05 - Ativar/Desativar Utilizador
     */
    public User setActive(Long id, boolean active) throws MyEntityNotFoundException {
        var user = find(id);
        user.setActive(active);
        em.flush();
        return user;
    }

    /**
     * Removes a user.
     * EP06 - Remover Utilizador
     */
    public void delete(Long id) throws MyEntityNotFoundException {
        var user = find(id);
        
        // Manual Cascade Delete/Unlink to avoid Constraint Violation
        
        // 1. Delete Activity Logs
        em.createQuery("DELETE FROM ActivityLog a WHERE a.user.id = :id")
          .setParameter("id", id)
          .executeUpdate();
          
        // 2. Unlink Publications (uploadedBy, submitter, hiddenBy)
        em.createQuery("UPDATE Publication p SET p.uploadedBy = null WHERE p.uploadedBy.id = :id")
          .setParameter("id", id)
          .executeUpdate();
        em.createQuery("UPDATE Publication p SET p.submitter = null WHERE p.submitter.id = :id")
          .setParameter("id", id)
          .executeUpdate();
        // Also clear hiddenBy if this user hid any publication
        em.createQuery("UPDATE Publication p SET p.hiddenBy = null WHERE p.hiddenBy.id = :id")
          .setParameter("id", id)
          .executeUpdate();

        // 3. Delete Comments (use 'author' not 'user') and Unlink hiddenBy
        em.createQuery("DELETE FROM Comment c WHERE c.author.id = :id")
          .setParameter("id", id)
          .executeUpdate();
        em.createQuery("UPDATE Comment c SET c.hiddenBy = null WHERE c.hiddenBy.id = :id")
          .setParameter("id", id)
          .executeUpdate();

        // 4. Delete Ratings (use 'user')
        em.createQuery("DELETE FROM Rating r WHERE r.user.id = :id")
          .setParameter("id", id)
          .executeUpdate();

        em.remove(user);
    }

    public boolean canLogin(String username, String password) {
        var user = findByUsername(username);
        if (user == null || !user.isActive()) {
            return false;
        }
        // Use Hasher to verify password
        return Hasher.verify(password, user.getPassword());
    }

    /**
     * Updates last login timestamp.
     */
    public void updateLastLogin(String username) {
        var user = findByUsername(username);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
        }
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        var user = findByUsername(username);
        if (user == null) return false;

        // Verify old password using Hasher
        if (!Hasher.verify(oldPassword, user.getPassword())) {
            return false;
        }

        // Hash and set new password
        user.setPassword(Hasher.hash(newPassword));
        em.merge(user);
        return true;
    }
    
    /**
     * Sets a new password for a user (used in password reset).
     * The password will be hashed before storing.
     */
    public void setPassword(Long userId, String newPassword) throws MyEntityNotFoundException {
        var user = find(userId);
        user.setPassword(Hasher.hash(newPassword));
        em.merge(user);
    }

    /**
     * Checks if a user has a specific permission.
     * Used by AuthorizationFilter for @RequirePermission and @RequireAction annotations.
     */
    public boolean userHasPermission(String username, String permission) {
        var user = findByUsername(username);
        if (user == null) return false;

        // Simple permission model:
        // - ADMINISTRADOR has all permissions
        // - RESPONSAVEL has most permissions except admin-only ones
        // - COLABORADOR has basic permissions
        if (user.getRole() == null) return false;

        switch (user.getRole()) {
            case ADMINISTRADOR:
                return true; // Admin has all permissions
            case RESPONSAVEL:
                // Manager has all permissions except user management
                return !permission.toLowerCase().contains("admin");
            case COLABORADOR:
                // Collaborator has basic permissions
                return permission.toLowerCase().contains("read") ||
                       permission.toLowerCase().contains("view") ||
                       permission.toLowerCase().contains("own");
            default:
                return false;
        }
    }
}