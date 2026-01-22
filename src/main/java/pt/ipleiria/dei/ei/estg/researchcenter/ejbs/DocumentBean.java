package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Document;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Stateless
public class DocumentBean {

    private static final String UPLOAD_DIR = "/tmp/uploads";

    @PersistenceContext
    private EntityManager em;

    @EJB
    private PublicationBean publicationBean;

    public Document create(String filename, Long publicationId, InputStream stream)
            throws MyEntityNotFoundException, IOException {

        var publication = publicationBean.find(publicationId);

        // Create directory for publication documents
        var targetDirectoryPath = Paths.get(UPLOAD_DIR, "publications");
        if (!Files.exists(targetDirectoryPath)) {
            Files.createDirectories(targetDirectoryPath);
        }

        // Generate unique filename to avoid conflicts
        var fileExtension = filename.substring(filename.lastIndexOf("."));
        var uniqueFileName = "pub_" + publicationId + "_" + UUID.randomUUID() + fileExtension;
        var targetFilePath = targetDirectoryPath.resolve(uniqueFileName);

        // Save file to disk
        Files.copy(stream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document entity
        var document = new Document(filename, targetFilePath.toString(), publication);
        em.persist(document);

        // Link to publication
        publication.setDocument(document);

        return document;
    }

    public Document find(Long id) throws MyEntityNotFoundException {
        var document = em.find(Document.class, id);
        if (document == null) {
            throw new MyEntityNotFoundException("Document with id " + id + " not found");
        }
        return document;
    }

    public Document findByPublication(Long publicationId) throws MyEntityNotFoundException {
        var documents = em.createNamedQuery("getPublicationDocument", Document.class)
                .setParameter("publicationId", publicationId)
                .getResultList();

        if (documents.isEmpty()) {
            throw new MyEntityNotFoundException("No document found for publication " + publicationId);
        }
        return documents.get(0);
    }

    public void update(String filename, Long publicationId, InputStream stream) throws MyEntityNotFoundException, IOException {
        // Check if exists
        try {
            var existingDoc = findByPublication(publicationId);
            // Delete old file
            var oldPath = Paths.get(existingDoc.getFilepath());
            if (Files.exists(oldPath)) {
                Files.delete(oldPath);
            }
            em.remove(existingDoc);
            em.flush();
        } catch (MyEntityNotFoundException ignore) {
            // No existing document, proceed to create
        }
        
        create(filename, publicationId, stream);
    }

    public void delete(Long id) throws MyEntityNotFoundException, IOException {
        var document = find(id);

        // Delete file from disk
        var filePath = Paths.get(document.getFilepath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Remove entity
        em.remove(document);
    }
}