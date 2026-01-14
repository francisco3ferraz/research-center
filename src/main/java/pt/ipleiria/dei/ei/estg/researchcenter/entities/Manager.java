package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.Entity;

@Entity
public class Manager extends User {
    
    // Default constructor
    public Manager() {
        super();
    }
    
    // Constructor with parameters
    public Manager(String username, String password, String name, String email) {
        super(username, password, name, email, UserRole.RESPONSAVEL);
    }
}