package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import java.io.ByteArrayInputStream;

@Startup
@Singleton
public class ConfigBean {
    
    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");
    
    @EJB
    private CollaboratorBean collaboratorBean;
    
    @EJB
    private ManagerBean managerBean;
    
    @EJB
    private AdministratorBean administratorBean;
    
    @EJB
    private TagBean tagBean;
    
    @EJB
    private ScientificAreaBean scientificAreaBean;
    
    @EJB
    private PublicationBean publicationBean;
    
    @EJB
    private CommentBean commentBean;
    
    @EJB
    private RatingBean ratingBean;
    
    @EJB
    private ActivityLogBean activityLogBean;

    @EJB
    private UserBean userBean;
    
    @EJB
    private DocumentBean documentBean;
    
    @EJB
    private NotificationBean notificationBean;
    
    @PostConstruct
    public void populateDB() {
        logger.info("Database population started...");
        
        try {
            // 1. Create Scientific Areas
            createScientificArea("Artificial Intelligence", "Study of intelligent agents and machine learning.");
            createScientificArea("Software Engineering", "Systematic application of engineering to software development.");
            createScientificArea("Cybersecurity", "Protection of computer systems and networks.");
            createScientificArea("Data Science", " extracting knowledge and insights from noisy, structured and unstructured data.");
            createScientificArea("Cloud Computing", "On-demand availability of computer system resources.");
            
            // 2. Create Tags
            createTag("Machine Learning", "ML algorithms and applications");
            createTag("Deep Learning", "Neural networks with many layers");
            createTag("IoT", "Internet of Things devices and networks");
            createTag("Web Development", "Building and maintaining websites");
            createTag("Network Security", "Policies and practices to prevent and monitor unauthorized access");
            createTag("Big Data", "Analysis of large and complex data sets");

            // 3. Create Users
            // Admin
            try {
                administratorBean.create("admin", "admin", "Administrator", "admin@research.com");
            } catch (Exception e) { logger.warning("Admin already exists usually: " + e.getMessage()); }
            
            // Manager
            try {
                managerBean.create("manager", "manager", "Manager Guy", "manager@research.com");
            } catch (Exception e) { logger.warning("Manager already exists usually: " + e.getMessage()); }

            // Collaborators
            createCollaborator("joao.silva", "password", "João Silva", "joao.silva@research.com");
            createCollaborator("maria.santos", "password", "Maria Santos", "maria.santos@research.com");
            createCollaborator("pedro.almeida", "password", "Pedro Almeida", "pedro.almeida@research.com");
            createCollaborator("ana.costa", "password", "Ana Costa", "ana.costa@research.com");

            // 4. Create Publications
            // Get collaborators for assignment
            Collaborator joao = collaboratorBean.findByUsername("joao.silva");
            Collaborator maria = collaboratorBean.findByUsername("maria.santos");
            Collaborator pedro = collaboratorBean.findByUsername("pedro.almeida");

            // Pub 1
            createPublication(
                "Deep Learning in Medical Diagnosis",
                Arrays.asList("João Silva", "Maria Santos"),
                PublicationType.ARTICLE,
                "Artificial Intelligence",
                2023,
                "This paper explores the application of deep convolutional neural networks in analyzing medical imaging for early cancer detection. Results show a 95% accuracy rate compared to traditional methods.",
                joao,
                Arrays.asList("Machine Learning", "Deep Learning"),
                true, false
            );

            // Pub 2
            createPublication(
                "Secure IoT Architectures for Smart Cities",
                Arrays.asList("Pedro Almeida", "John Doe"),
                PublicationType.CONFERENCE,
                "Cybersecurity",
                2024,
                "We propose a novel blockchain-based security framework for IoT devices in smart city infrastructures. The framework addresses key challenges in authentication and data integrity.",
                pedro,
                Arrays.asList("IoT", "Network Security"),
                true, false
            );

            // Pub 3
            createPublication(
                "Microservices vs Monoliths: A Performance Analysis",
                Arrays.asList("Ana Costa", "João Silva"),
                PublicationType.TECHNICAL_REPORT,
                "Software Engineering",
                2022,
                "A comparative study of performance metrics between microservices and monolithic architectures in high-load e-commerce applications.",
                joao, // uploaded by joao even if ana is first author
                Arrays.asList("Web Development", "Cloud Computing"),
                true, false
            );

            // Pub 4 (Confidential)
            createPublication(
                "Internal Algorithm for Predictive Maintenance",
                Arrays.asList("Internal Team"),
                PublicationType.PATENT,
                "Data Science",
                2024,
                "Proprietary algorithm for predicting machinery failure in industrial settings. STRICTLY CONFIDENTIAL.",
                maria,
                Arrays.asList("Big Data", "Machine Learning"),
                true, true
            );

            // Pub 5
            createPublication(
                "The Future of Cloud Native Applications",
                Arrays.asList("Maria Santos"),
                PublicationType.BOOK_CHAPTER,
                "Cloud Computing",
                2021,
                "This chapter discusses the evolution of cloud native technologies and what to expect in the next decade regarding serverless computing.",
                maria,
                Arrays.asList("Cloud Computing"),
                true, false
            );
            
             // Pub 6
            createPublication(
                "Ethical Implications of AI",
                Arrays.asList("João Silva", "Philosopher X"),
                PublicationType.ARTICLE,
                "Artificial Intelligence",
                2023,
                "Discussing the bias and ethical considerations in modern AI models.",
                joao,
                Arrays.asList("Machine Learning"),
                true, false
            );

            // 5. Add Interactions (Comments & Ratings) & Subscriptions & Logs
            List<Publication> pubs = publicationBean.findAll();
            
            // Subscriptions
            try {
                // Joao follows Machine Learning
                Tag ml = tagBean.findByName("Machine Learning");
                collaboratorBean.subscribeToTag(joao.getId(), ml.getId());
                activityLogBean.createWithChangedFields(joao, "SUBSCRIBE", "TAG", ml.getId(), "Subscribed to Machine Learning", "subscription");
                
                // Maria follows Cloud Computing
                Tag cc = tagBean.findByName("Cloud Computing");
                collaboratorBean.subscribeToTag(maria.getId(), cc.getId());
                activityLogBean.createWithChangedFields(maria, "SUBSCRIBE", "TAG", cc.getId(), "Subscribed to Cloud Computing", "subscription");
            } catch (Exception e) { logger.warning("Subscription failed: " + e.getMessage()); }

            if (!pubs.isEmpty()) {
                Publication p1 = pubs.get(0); // Deep Learning
                 // Maria comments on Joao's paper
                try {
                    commentBean.create("Excellent research! Have you considered larger datasets?", maria.getId(), p1.getId());
                    activityLogBean.createWithChangedFields(maria, "ADD_COMMENT", "PUBLICATION", p1.getId(), "Commented on " + p1.getTitle(), "comment");
                    
                    // Notification for Joao
                    notificationBean.create(joao.getId(), "NEW_COMMENT", "New Comment", "Maria Santos commented on your publication", "PUBLICATION", p1.getId());
                    
                     // Pedro rates it
                    ratingBean.create(5, pedro.getId(), p1.getId());
                    activityLogBean.createWithChangedFields(pedro, "ADD_RATING", "PUBLICATION", p1.getId(), "Rated " + p1.getTitle(), "stars");

                } catch (Exception e) { logger.info("Interaction already exists or failed: " + e.getMessage()); }
                
                // More interactions
                if (pubs.size() > 2) {
                    Publication p3 = pubs.get(2); // Microservices
                    try {
                        commentBean.create("I disagree with the conclusion regarding latency.", pedro.getId(), p3.getId());
                        ratingBean.create(4, maria.getId(), p3.getId());
                        
                        // Ana (author) gets notification
                        notificationBean.create(collaboratorBean.findByUsername("ana.costa").getId(), "NEW_RATING", "New Rating", "Maria Santos rated your publication", "PUBLICATION", p3.getId());
                        
                    } catch (Exception e) {}
                }
            }

            // 6. Seed Notifications (General)
            try {
                notificationBean.create(joao.getId(), "SYSTEM", "Welcome", "Welcome to the Research Center!", "USER", joao.getId());
                notificationBean.markAllAsRead(joao.getId()); // Mark welcome as read
                
                notificationBean.create(joao.getId(), "ALERT", "Profile Update", "Please update your profile information.", "USER", joao.getId());
            } catch (Exception e) {}

            logger.info("Massive seeding completed.");
            
        } catch (Exception e) {
            logger.severe("Critical error in seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createScientificArea(String name, String desc) {
        try {
            scientificAreaBean.create(name, desc);
        } catch (Exception e) {
            logger.info("Area " + name + " exists or failed: " + e.getMessage());
        }
    }

    private void createTag(String name, String desc) {
        try {
            tagBean.create(name, desc);
        } catch (Exception e) {
            logger.info("Tag " + name + " exists or failed: " + e.getMessage());
        }
    }

    private void createCollaborator(String username, String password, String name, String email) {
        try {
            collaboratorBean.create(username, password, name, email);
        } catch (Exception e) {
            logger.info("Collaborator " + username + " exists or failed: " + e.getMessage());
        }
    }

    private void createPublication(String title, List<String> authors, PublicationType type,
                                 String area, Integer year, String abstract_,
                                 Collaborator uploader, List<String> tagNames,
                                 boolean visible, boolean confidential) {
        try {
            // Check if similar title exists to avoid duplicates on every redeploy?
            // simple check
            List<Publication> existing = publicationBean.findWithFilters(title, null, null, null, null, 0, 1);
            if (!existing.isEmpty() && existing.get(0).getTitle().equals(title)) {
                return;
            }

            Publication p = publicationBean.create(title, authors, type, area, year, abstract_, uploader.getId());
            
            if (confidential) {
                publicationBean.update(p.getId(), title, authors, abstract_, null, year, null, null, visible, true);
            }
            
            // Add tags
            for (String tagName : tagNames) {
                try {
                    Tag t = tagBean.findByName(tagName);
                    publicationBean.addTag(p.getId(), t.getId());
                } catch (Exception e) {
                    logger.warning("Tag not found for linking: " + tagName);
                }
            }
            
            // Create dummy doc
            documentBean.create("document.pdf", p.getId(), new ByteArrayInputStream(createDummyPdf(title)));

        } catch (Exception e) {
            logger.warning("Failed to create publication " + title + ": " + e.getMessage());
        }
    }
    
    private byte[] createDummyPdf(String title) {
        // Create a minimal valid PDF with the publication title
        String pdfContent = "%PDF-1.4\n" +
            "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
            "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> " +
            "/MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n" +
            "4 0 obj\n<< /Length 44 >>\nstream\nBT /F1 12 Tf 50 700 Td (" + title.replace("(", "").replace(")", "") + ") Tj ET\nendstream\nendobj\n" +
            "xref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n" +
            "0000000300 00000 n\ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n380\n%%EOF";
        return pdfContent.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }
}