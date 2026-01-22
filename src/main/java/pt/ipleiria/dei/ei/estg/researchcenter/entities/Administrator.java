package pt.ipleiria.dei.ei.estg.researchcenter.entities;

import jakarta.persistence.Entity;

@Entity
public class Administrator extends Manager {
    
    // Default constructor
    public Administrator() {
        super();
    }
    
    // Constructor with parameters
    public Administrator(String username, String password, String name, String email) {
        super(username, password, name, email, UserRole.ADMINISTRADOR);
    }
}