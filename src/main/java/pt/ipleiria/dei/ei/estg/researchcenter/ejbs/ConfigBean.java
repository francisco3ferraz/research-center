package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.logging.Logger;

@Startup
@Singleton
public class ConfigBean {

    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @EJB
    private ScientificAreaBean scientificAreaBean;

    @EJB
    private CollaboratorBean collaboratorBean;

    @EJB
    private ManagerBean managerBean;

    @EJB
    private AdministratorBean administratorBean;

    @EJB
    private TagBean tagBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private RatingBean ratingBean;

    @PostConstruct
    public void populateDB() {
        System.out.println("Hello Publications Platform!");
        logger.info("Database population started...");

        try {
            // Create Scientific Areas
            scientificAreaBean.create("Data Science");
            scientificAreaBean.create("Material Science");
            scientificAreaBean.create("Artificial Intelligence");
            scientificAreaBean.create("Quantum Computing");
            logger.info("Scientific areas created");

            // Create Tags
            tagBean.create("Projeto X");
            tagBean.create("Projeto Y");
            tagBean.create("Machine Learning");
            tagBean.create("Deep Learning");
            tagBean.create("Urgent");
            logger.info("Tags created");

            // Create Users
            administratorBean.create("admin", "admin123", "Administrator User", "admin@research.pt");

            managerBean.create("manager1", "manager123", "Maria Manager", "maria@research.pt");

            collaboratorBean.create("joao", "joao123", "João A", "joao@research.pt");
            collaboratorBean.create("joana", "joana123", "Joana B", "joana@research.pt");
            collaboratorBean.create("manuel", "manuel123", "Manuel C", "manuel@research.pt");
            collaboratorBean.create("ana", "ana123", "Ana D", "ana@research.pt");
            logger.info("Users created");

            // Create Publications
            Long pub1 = publicationBean.create(
                    "Machine Learning in Data Science",
                    "This paper explores the application of ML techniques in data analysis.",
                    "joao",
                    1L // Data Science
            ).getId();

            Long pub2 = publicationBean.create(
                    "Quantum Materials Research",
                    "A comprehensive study on quantum properties of new materials.",
                    "joana",
                    2L // Material Science
            ).getId();

            Long pub3 = publicationBean.create(
                    "Deep Learning for Image Recognition",
                    "Novel approach to image classification using deep neural networks.",
                    "manuel",
                    3L // AI
            ).getId();

            logger.info("Publications created");

            // Add tags to publications
            publicationBean.addTag(pub1, 1L); // Projeto X
            publicationBean.addTag(pub1, 3L); // Machine Learning

            publicationBean.addTag(pub2, 2L); // Projeto Y

            publicationBean.addTag(pub3, 1L); // Projeto X
            publicationBean.addTag(pub3, 3L); // Machine Learning
            publicationBean.addTag(pub3, 4L); // Deep Learning
            publicationBean.addTag(pub3, 5L); // Urgent

            logger.info("Tags added to publications");

            // Create Comments
            commentBean.create(
                    "Parem com tudo! Esta nova técnica pode revolucionar a nossa abordagem ao Projeto X!",
                    "joana",
                    pub1
            );

            commentBean.create(
                    "Excelente trabalho! Muito relevante para o Projeto X.",
                    "manuel",
                    pub1
            );

            commentBean.create(
                    "Esta abordagem precisa de mais validação experimental.",
                    "ana",
                    pub2
            );

            logger.info("Comments created");

            // Create Ratings
            ratingBean.create(5, "joao", pub2);
            ratingBean.create(4, "joana", pub1);
            ratingBean.create(5, "manuel", pub1);
            ratingBean.create(4, "ana", pub3);
            ratingBean.create(5, "joao", pub3);

            logger.info("Ratings created");

            // Subscribe users to tags
            collaboratorBean.subscribeToTag("joao", 1L); // João subscribes to Projeto X
            collaboratorBean.subscribeToTag("manuel", 1L); // Manuel subscribes to Projeto X
            collaboratorBean.subscribeToTag("ana", 2L); // Ana subscribes to Projeto Y

            logger.info("Tag subscriptions created");

        } catch (Exception e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("Database population completed!");
    }
}