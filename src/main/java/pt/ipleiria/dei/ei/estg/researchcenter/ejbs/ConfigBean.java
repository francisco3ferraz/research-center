package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType;

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
    
    @PostConstruct
    public void populateDB() {
        System.out.println("Hello Publications Platform! Starting Massive Seeding...");
        logger.info("Database population started...");
        
        try {
            // 1. Create Users
            // Helper to safe create
            pt.ipleiria.dei.ei.estg.researchcenter.entities.Administrator admin = null;
            try { admin = administratorBean.create("admin", "admin", "Administrator", "admin@research.pt"); } catch (Exception e) { 
                 try { admin = (pt.ipleiria.dei.ei.estg.researchcenter.entities.Administrator) userBean.findByUsername("admin"); } catch(Exception ex) {} 
            }
            
            pt.ipleiria.dei.ei.estg.researchcenter.entities.Manager responsavel = null;
            try { responsavel = managerBean.create("responsavel", "123", "Responsável T.", "resp@research.pt"); } catch (Exception e) {
                 try { responsavel = (pt.ipleiria.dei.ei.estg.researchcenter.entities.Manager) userBean.findByUsername("responsavel"); } catch(Exception ex) {}
            }

            var collaborators = new java.util.ArrayList<pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator>();
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
            var allTags = new java.util.ArrayList<pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag>();
            for (int i = 1; i <= 20; i++) {
                try {
                    allTags.add(tagBean.create("Tag " + i, "Description for Tag " + i));
                } catch (Exception e) {
                    try { allTags.add(tagBean.findByName("Tag "+i)); } catch(Exception ex){}
                }
            }

            // 4. Create Publications (Massive Loop)
            java.util.Random rand = new java.util.Random();
            var types = PublicationType.values();
            
            if (collaborators.isEmpty()) { 
                logger.warning("No collaborators found, skipping publication seeding.");
                return; 
            }

            logger.info("Seeding 200 publications...");
            for (int i = 1; i <= 200; i++) {
                try {
                    var uploader = collaborators.get(rand.nextInt(collaborators.size()));
                    var type = types[rand.nextInt(types.length)];
                    var area = areaNames[rand.nextInt(areaNames.length)];
                    int year = 2015 + rand.nextInt(11); // 2015-2025
                    
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
                            "Upload da publicação '" + title + "'");
                    } catch (Exception ignored) {}
                    
                    // Set Random Views (0 - 1200)
                    pub.setViewsCount(rand.nextInt(1201));

                    // Add Random Tags (1 to 5 tags)
                    int numTags = 1 + rand.nextInt(5);
                    for (int t = 0; t < numTags; t++) {
                        if (!allTags.isEmpty()) {
                            publicationBean.addTag(pub.getId(), allTags.get(rand.nextInt(allTags.size())).getId());
                        }
                    }
                    
                    // Add Comments (0-20)
                    int numComments = rand.nextInt(21);
                    for (int c = 0; c < numComments; c++) {
                        var author = collaborators.get(rand.nextInt(collaborators.size()));
                        try {
                            commentBean.create("Comment " + c + " on " + title + ". Interesting work!", author.getId(), pub.getId());
                            activityLogBean.create(author, "COMMENT", "PUBLICATION", pub.getId(), "Comentou na publicação '" + title + "'");
                        } catch(Exception ignored){}
                    }
                    
                    // Add Ratings (0-25)
                    int numRatings = rand.nextInt(26);
                    for (int r = 0; r < numRatings; r++) {
                        var author = collaborators.get(rand.nextInt(collaborators.size()));
                        try {
                            ratingBean.create(1 + rand.nextInt(5), author.getId(), pub.getId());
                            activityLogBean.create(author, "RATE", "PUBLICATION", pub.getId(), "Avaliou a publicação '" + title + "'");
                        } catch (Exception ignored){}
                    }
                    
                } catch (Exception e) {
                   // Ignore specific errors (duplicates etc)
                }
            }
            logger.info("Massive seeding completed.");
            
        } catch (Exception e) {
            logger.severe("Critical error in seeding: " + e.getMessage());
            e.printStackTrace();
        }
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