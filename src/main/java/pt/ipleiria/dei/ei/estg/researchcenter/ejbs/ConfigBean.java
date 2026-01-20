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
            // 1. Create Users
            // Helper to safe create
            Administrator admin = null;
            try { admin = administratorBean.create("admin", "admin", "Administrator", "admin@research.pt"); } catch (Exception e) { 
                 try { admin = (Administrator) userBean.findByUsername("admin"); } catch(Exception ex) {} 
            }
            
            Manager responsavel = null;
            try { responsavel = managerBean.create("responsavel", "123", "Responsável T.", "resp@research.pt"); } catch (Exception e) {
                 try { responsavel = (Manager) userBean.findByUsername("responsavel"); } catch(Exception ex) {}
            }

            var collaborators = new ArrayList<Collaborator>();
            for (int i = 1; i <= 10; i++) {
                try {
                    collaborators.add(collaboratorBean.create("user" + i, "123", "Collaborator " + i, "user" + i + "@research.pt"));
                } catch (Exception e) {
                    try { collaborators.add(collaboratorBean.findByUsername("user"+i)); } catch(Exception ex) {}
                }
            }
            
            // 2. Create Scientific Areas
            String[] areaNames = {"Software Engineering", "Artificial Intelligence", "Cybersecurity", "Internet of Things", "Data Science", "Bioinformatics", "Robotics"};
            for (String name : areaNames) {
                try { scientificAreaBean.create(name, "Research in " + name); } catch (Exception ignored) {}
            }
            
            // 3. Create Tags
            var allTags = new ArrayList<Tag>();
            for (int i = 1; i <= 20; i++) {
                try {
                    allTags.add(tagBean.create("Tag " + i, "Description for Tag " + i));
                } catch (Exception e) {
                    try { allTags.add(tagBean.findByName("Tag "+i)); } catch(Exception ex){}
                }
            }

            // 4. Create Publications (Massive Loop)
            var rand = new java.util.Random();
            var types = PublicationType.values();
            
            if (collaborators.isEmpty()) { 
                logger.warning("No collaborators found, skipping publication seeding.");
                return; 
            }

            logger.info("Seeding 50 publications...");
            for (int i = 1; i <= 50; i++) {
                try {
                    var uploader = collaborators.get(rand.nextInt(collaborators.size()));
                    var type = types[rand.nextInt(types.length)];
                    var area = areaNames[rand.nextInt(areaNames.length)];
                    var year = 2015 + rand.nextInt(11); // 2015-2025
                    
                    String title = "Publication " + i + ": " + generateRandomTitle(rand);
                    String summary = "This is a generated summary for publication " + i + ". It explores " + area + " in the context of " + type.getName() + "...";
                    
                    var pub = publicationBean.create(
                        title,
                        Arrays.asList(uploader.getName(), "Co-Author A", "Co-Author B"),
                        type,
                        area,
                        year,
                        summary,
                        uploader.getId()
                    );
                    
                    // Metadata
                    pub.setDoi("10.1000/" + rand.nextInt(10000));
                    pub.setPublisher(rand.nextBoolean() ? "IEEE" : (rand.nextBoolean() ? "ACM" : "Springer"));
                    if (rand.nextBoolean()) pub.setAiGeneratedSummary("AI Summary: " + summary);
                    
                    // Visibility (10% hidden, 5% confidential)
                    if (rand.nextInt(100) < 10) pub.setVisible(false);
                    if (rand.nextInt(100) < 5) pub.setConfidential(true);
                    
                    // Activity Log for Upload
                    try {
                        activityLogBean.create(uploader, "UPLOAD_PUBLICATION", "PUBLICATION", pub.getId(), 
                            "Upload of publication '" + title + "'");
                    } catch (Exception ignored) {}
                    
                    // Set Random Views (0 - 1200)
                    pub.setViewsCount(rand.nextInt(1201));
                    
                    // Add PDF document to 30% of publications
                    if (rand.nextInt(100) < 30) {
                        try {
                            // Create a dummy PDF document
                            byte[] dummyPdf = createDummyPdf(title);
                            var inputStream = new java.io.ByteArrayInputStream(dummyPdf);
                            documentBean.create("publication_" + i + ".pdf", pub.getId(), inputStream);
                        } catch (Exception ignored) {}
                    }

                    // Add Random Tags (1 to 5 tags)
                    var numTags = 1 + rand.nextInt(5);
                    for (int t = 0; t < numTags; t++) {
                        if (!allTags.isEmpty()) {
                            publicationBean.addTag(pub.getId(), allTags.get(rand.nextInt(allTags.size())).getId());
                        }
                    }
                    
                    // Add Comments (0-20)
                    var numComments = rand.nextInt(21);
                    for (int c = 0; c < numComments; c++) {
                        var author = collaborators.get(rand.nextInt(collaborators.size()));
                        try {
                            commentBean.create("Comment " + c + " on " + title + ". Interesting work!", author.getId(), pub.getId());
                            activityLogBean.create(author, "COMMENT", "PUBLICATION", pub.getId(), "Commented on publication '" + title + "'");
                        } catch(Exception ignored){}
                    }
                    
                    // Add Ratings (0-25)
                    var numRatings = rand.nextInt(26);
                    for (int r = 0; r < numRatings; r++) {
                        var author = collaborators.get(rand.nextInt(collaborators.size()));
                        try {
                            ratingBean.create(1 + rand.nextInt(5), author.getId(), pub.getId());
                            activityLogBean.create(author, "RATE", "PUBLICATION", pub.getId(), "Rated publication '" + title + "'");
                        } catch (Exception ignored){}
                    }
                    
                } catch (Exception e) {
                   // Ignore specific errors (duplicates etc)
                }
            }
            
            // 5. Create Random Notifications for each collaborator
            logger.info("Seeding notifications for collaborators...");
            String[] notificationTypes = {"NEW_PUBLICATION_WITH_TAG", "NEW_COMMENT_ON_TAG", "NEW_RATING", "SYSTEM"};
            String[] notificationTitles = {
                "New Subscription Publication",
                "New Comment Notification",
                "New Rating Received",
                "Welcome to Research Center!",
                "System Update",
                "New Feature Available",
                "Featured Publication",
                "Collaboration Invite"
            };
            String[] notificationMessages = {
                "A new publication has been added with a tag you subscribed to.",
                "A new comment was added to a publication with a tag you subscribed to.",
                "Your publication received a new rating.",
                "Thanks for joining our platform. Explore publications and subscribe to tags of interest.",
                "The system has been updated with new features.",
                "You can now export publications to PDF format.",
                "A publication that might interest you was featured by the community.",
                "You have been invited to collaborate on a new research project."
            };
            
            for (Collaborator collaborator : collaborators) {
                // Create 3-8 random notifications per user
                int numNotifications = 3 + rand.nextInt(6);
                for (int n = 0; n < numNotifications; n++) {
                    try {
                        String type = notificationTypes[rand.nextInt(notificationTypes.length)];
                        String title = notificationTitles[rand.nextInt(notificationTitles.length)];
                        String message = notificationMessages[rand.nextInt(notificationMessages.length)];
                        
                        // Determine related entity based on type
                        String relatedEntityType = "SYSTEM";
                        Long relatedEntityId = null;
                        
                        if (type.equals("NEW_PUBLICATION_WITH_TAG") || type.equals("NEW_COMMENT_ON_TAG") || type.equals("NEW_RATING")) {
                            relatedEntityType = "PUBLICATION";
                            relatedEntityId = (long) (1 + rand.nextInt(50)); // Random publication ID
                        }
                        
                        // Create notification
                        var notification = notificationBean.create(
                            collaborator.getId(),
                            type,
                            title,
                            message,
                            relatedEntityType,
                            relatedEntityId
                        );
                        
                        // Mark some as read (60% chance)
                        if (rand.nextInt(100) < 60) {
                            notificationBean.markAsRead(notification.getId());
                        }
                    } catch (Exception e) {
                        // Ignore errors for individual notifications
                    }
                }
            }
            logger.info("Notification seeding completed.");
            
            logger.info("Massive seeding completed.");
            
        } catch (Exception e) {
            logger.severe("Critical error in seeding: " + e.getMessage());
            e.printStackTrace();
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
    
    private String generateRandomTitle(java.util.Random rand) {
        String[] prefixes = {"Advanced", "Novel", "Study of", "Analysis of", "Review of", "Future of"};
        String[] subjects = {"Machine Learning", "Cloud Computing", "IoT", "Blockchain", "Quantum Computing", "Bio-Algorithms"};
        String[] suffixes = {"Systems", "Networks", "Applications", "Challenges", "Protocols"};
        
        return prefixes[rand.nextInt(prefixes.length)] + " " +
               subjects[rand.nextInt(subjects.length)] + " " +
               suffixes[rand.nextInt(suffixes.length)];
    }
}