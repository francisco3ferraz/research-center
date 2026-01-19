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
    
    @PostConstruct
    public void populateDB() {
        System.out.println("Hello Publications Platform!");
        logger.info("Database population started...");
        
        try {
            // Create Tags
            tagBean.create("Projeto X", "Publicações relacionadas com o Projeto X");
            tagBean.create("Projeto Y", "Publicações relacionadas com o Projeto Y");
            tagBean.create("Machine Learning", "Técnicas e aplicações de ML");
            tagBean.create("Deep Learning", "Redes neurais profundas");
            tagBean.create("Urgent", "Conteúdo urgente");
            logger.info("Tags created");
            
            // Create Users
            var admin = administratorBean.create("admin", "admin", "Administrator User", "admin@research.pt");
            logger.info("Administrator created with ID: " + admin.getId());
            
            var manager = managerBean.create("manager1", "manager", "Maria Manager", "maria@research.pt");
            logger.info("Manager created with ID: " + manager.getId());
            
            var joao = collaboratorBean.create("joao", "joao123", "João A", "joao@research.pt");
            var joana = collaboratorBean.create("joana", "joana123", "Joana B", "joana@research.pt");
            var manuel = collaboratorBean.create("manuel", "manuel123", "Manuel C", "manuel@research.pt");
            var ana = collaboratorBean.create("ana", "ana123", "Ana D", "ana@research.pt");
            logger.info("Collaborators created");

            // Ensure a system collaborator exists to be used as a fallback uploader
            try {
                collaboratorBean.findByUsername("system");
            } catch (pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException e) {
                try {
                    collaboratorBean.create("system", "system", "System", "system@research.pt");
                    logger.info("System collaborator created");
                } catch (Exception ex) {
                    logger.warning("Could not create system collaborator: " + ex.getMessage());
                }
            }
            
            // Create Publications
            // Ensure some scientific areas exist for selects and associations
            try {
                scientificAreaBean.create("Ciência de Dados", "Área relacionada com análise de dados e machine learning");
            } catch (Exception ignored) {}
            try {
                scientificAreaBean.create("Ciência dos Materiais", "Estudo de materiais e propriedades quânticas");
            } catch (Exception ignored) {}
            try {
                scientificAreaBean.create("Inteligência Artificial", "Pesquisa em IA, redes neurais e aprendizagem automática");
            } catch (Exception ignored) {}
            try {
                scientificAreaBean.create("Segurança Informática", "Segurança, redes e criptografia");
            } catch (Exception ignored) {}
            try {
                scientificAreaBean.create("Sistemas Distribuídos", "Sistemas, redes e computação distribuída");
            } catch (Exception ignored) {}
            var pub1 = publicationBean.create(
                "Machine Learning in Data Science",
                Arrays.asList("João A", "Maria Santos"),
                PublicationType.ARTICLE,
                "Ciência de Dados",
                2024,
                "This paper explores the application of ML techniques in data analysis.",
                joao.getId()
            );
            pub1.setPublisher("IEEE");
            pub1.setDoi("10.1109/example.2024.123456");
            pub1.setAiGeneratedSummary("Este artigo apresenta técnicas inovadoras de machine learning aplicadas à ciência de dados...");
            
            var pub2 = publicationBean.create(
                "Quantum Materials Research",
                Arrays.asList("Joana B", "Pedro Silva"),
                PublicationType.ARTICLE,
                "Ciência dos Materiais",
                2024,
                "A comprehensive study on quantum properties of new materials.",
                joana.getId()
            );
            pub2.setPublisher("Nature");
            pub2.setDoi("10.1038/example.2024.789");
            
            var pub3 = publicationBean.create(
                "Deep Learning for Image Recognition",
                Arrays.asList("Manuel C"),
                PublicationType.CONFERENCE,
                "Inteligência Artificial",
                2025,
                "Novel approach to image classification using deep neural networks.",
                manuel.getId()
            );
            pub3.setPublisher("ACM");
            pub3.setConfidential(false);
            
            logger.info("Publications created");
            
            // Add tags to publications
            publicationBean.addTag(pub1.getId(), 1L); // Projeto X
            publicationBean.addTag(pub1.getId(), 3L); // Machine Learning
            
            publicationBean.addTag(pub2.getId(), 2L); // Projeto Y
            
            publicationBean.addTag(pub3.getId(), 1L); // Projeto X
            publicationBean.addTag(pub3.getId(), 3L); // Machine Learning
            publicationBean.addTag(pub3.getId(), 4L); // Deep Learning
            publicationBean.addTag(pub3.getId(), 5L); // Urgent
            
            logger.info("Tags added to publications");
            
            // Create Comments
            commentBean.create(
                "Parem com tudo! Esta nova técnica pode revolucionar a nossa abordagem ao Projeto X!",
                joana.getId(),
                pub1.getId()
            );
            
            commentBean.create(
                "Excelente trabalho! Muito relevante para o Projeto X.",
                manuel.getId(),
                pub1.getId()
            );
            
            commentBean.create(
                "Esta abordagem precisa de mais validação experimental.",
                ana.getId(),
                pub2.getId()
            );
            
            logger.info("Comments created");
            
            // Create Ratings
            ratingBean.create(5, joao.getId(), pub2.getId());
            ratingBean.create(4, joana.getId(), pub1.getId());
            ratingBean.create(5, manuel.getId(), pub1.getId());
            ratingBean.create(4, ana.getId(), pub3.getId());
            ratingBean.create(5, joao.getId(), pub3.getId());
            
            logger.info("Ratings created");
            
            // Subscribe users to tags
            collaboratorBean.subscribeToTag(joao.getId(), 1L); // João subscribes to Projeto X
            collaboratorBean.subscribeToTag(manuel.getId(), 1L); // Manuel subscribes to Projeto X
            collaboratorBean.subscribeToTag(ana.getId(), 2L); // Ana subscribes to Projeto Y
            
            logger.info("Tag subscriptions created");
            
            // Create some activity logs
            activityLogBean.create(admin, "CREATE_USER", "USER", joao.getId(),
                "Criação do utilizador 'João A'");
            activityLogBean.create(admin, "CREATE_USER", "USER", joana.getId(),
                "Criação do utilizador 'Joana B'");
            activityLogBean.create(admin, "CREATE_USER", "USER", manuel.getId(),
                "Criação do utilizador 'Manuel C'");
            activityLogBean.create(manager, "CREATE_TAG", "TAG", 1L,
                "Criação da tag 'Projeto X'");
            activityLogBean.create(joao, "UPLOAD_PUBLICATION", "PUBLICATION", pub1.getId(),
                "Upload da publicação 'Machine Learning in Data Science'");
            activityLogBean.create(joana, "UPLOAD_PUBLICATION", "PUBLICATION", pub2.getId(), 
                "Upload da publicação 'Quantum Materials Research'");
            activityLogBean.create(manuel, "ADD_COMMENT", "PUBLICATION", pub1.getId(), 
                "Comentário adicionado à publicação");
            
            logger.info("Activity logs created");
            
        } catch (Exception e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
        
        logger.info("Database population completed!");
    }
}