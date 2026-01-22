package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.Entity;

@Entity
public class Manager extends Collaborator {
    
    // Default constructor
    public Manager() {
        super();
    }
    
    // Constructor with parameters
    public Manager(String username, String password, String name, String email) {
        this(username, password, name, email, UserRole.RESPONSAVEL);
    }

    // Protected constructor for subclasses
    protected Manager(String username, String password, String name, String email, UserRole role) {
        super(username, password, name, email, role);
    }
}