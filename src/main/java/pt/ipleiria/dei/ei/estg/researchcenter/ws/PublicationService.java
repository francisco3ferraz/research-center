package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.DocumentBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.PublicationBean;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import jakarta.ws.rs.core.StreamingOutput;

@Path("publications")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class PublicationService {
    
    @EJB
    private PublicationBean publicationBean;
    @EJB
    private DocumentBean documentBean;
    
    @GET
    public Response getAll() {
        var publications = publicationBean.findAll();
        List<Publication> full = new ArrayList<>();
        for (Publication p : publications) {
            try {
                full.add(publicationBean.findWithDetails(p.getId()));
            } catch (Exception e) {
                // skip if cannot load details for a publication
            }
        }
        return Response.ok(PublicationDTO.from(full)).build();
    }
    
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) throws Exception {
        var dto = publicationBean.getDTOWithDetails(id);
        return Response.ok(dto).build();
    }
    
    @GET
    @Path("/uploaded-by/{userId}")
    public Response getByUploadedBy(@PathParam("userId") Long userId) throws Exception {
        var publications = publicationBean.findByUploadedBy(userId);
        return Response.ok(PublicationDTO.from(publications)).build();
    }
    
    @GET
    @Path("/area/{areaScientific}")
    public Response getByArea(@PathParam("areaScientific") String areaScientific) {
        var publications = publicationBean.findByAreaScientific(areaScientific);
        return Response.ok(PublicationDTO.from(publications)).build();
    }
    
    @GET
    @Path("/tag/{tagId}")
    public Response getByTag(@PathParam("tagId") Long tagId) throws Exception {
        var publications = publicationBean.findByTag(tagId);
        return Response.ok(PublicationDTO.from(publications)).build();
    }
    
    @POST
    public Response create(PublicationDTO dto) throws Exception {
        var publication = publicationBean.create(
            dto.getTitle(),
            dto.getAuthors(),
            dto.getType(),
            dto.getAreaScientific(),
            dto.getYear(),
            dto.getAbstract_(),
            dto.getUploadedById()
        );
        
        // Set optional fields if provided
        if (dto.getPublisher() != null || dto.getDoi() != null || dto.getAiGeneratedSummary() != null) {
            publicationBean.update(
                publication.getId(),
                dto.getTitle(),
                dto.getAuthors(),
                dto.getAbstract_(),
                dto.getAiGeneratedSummary(),
                dto.getYear(),
                dto.getPublisher(),
                dto.getDoi()
            );
        }
        
        return Response.status(Response.Status.CREATED)
                       .entity(PublicationDTO.from(publication))
                       .build();
    }

    @POST
    @Path("/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response createMultipart(MultipartFormDataInput input) throws Exception {
        Map<String, List<InputPart>> form = input.getFormDataMap();

        // Parse metadata
        String metadataJson = null;
        if (form.containsKey("metadata")) {
            var metaPart = form.get("metadata").get(0);
            metadataJson = metaPart.getBodyAsString();
        }

        Jsonb jsonb = JsonbBuilder.create();
        PublicationDTO dto = metadataJson != null ? jsonb.fromJson(metadataJson, PublicationDTO.class) : new PublicationDTO();

        var publication = publicationBean.create(
            dto.getTitle(),
            dto.getAuthors(),
            dto.getType(),
            dto.getAreaScientific(),
            dto.getYear(),
            dto.getAbstract_(),
            dto.getUploadedById()
        );

        if (dto.getPublisher() != null || dto.getDoi() != null || dto.getAiGeneratedSummary() != null) {
            publicationBean.update(
                publication.getId(),
                dto.getTitle(),
                dto.getAuthors(),
                dto.getAbstract_(),
                dto.getAiGeneratedSummary(),
                dto.getYear(),
                dto.getPublisher(),
                dto.getDoi()
            );
        }

        // Handle file upload if present
        if (form.containsKey("file")) {
            try {
                InputPart filePart = form.get("file").get(0);
                InputStream stream = filePart.getBody(InputStream.class, null);
                String contentDisposition = filePart.getHeaders().getFirst("Content-Disposition");
                String filename = "file";
                if (contentDisposition != null && contentDisposition.contains("filename=")) {
                    var idx = contentDisposition.indexOf("filename=");
                    filename = contentDisposition.substring(idx + 9).replaceAll("\"", "").trim();
                }
                documentBean.create(filename, publication.getId(), stream);
            } catch (IOException e) {
                // ignore file save errors for now or rethrow as 500
                throw e;
            }
        }

        var publicationDetails = publicationBean.findWithDetails(publication.getId());
        return Response.status(Response.Status.CREATED)
                       .entity(PublicationDTO.fromWithDetails(publicationDetails))
                       .build();
    }
    
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, PublicationDTO dto) throws Exception {
        publicationBean.update(
            id,
            dto.getTitle(),
            dto.getAuthors(),
            dto.getAbstract_(),
            dto.getAiGeneratedSummary(),
            dto.getYear(),
            dto.getPublisher(),
            dto.getDoi()
        );
        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws Exception {
        publicationBean.delete(id);
        return Response.ok(Map.of("message", "Publicação removida com sucesso")).build();
    }
    
    @POST
    @Path("/{id}/tags/{tagId}")
    public Response addTag(@PathParam("id") Long id, @PathParam("tagId") Long tagId) throws Exception {
        publicationBean.addTag(id, tagId);
        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }

    // Spec-compliant: accept JSON body { "tagId": 5 }
    @POST
    @Path("/{id}/tags")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addTagByBody(@PathParam("id") Long id, String rawBody) throws Exception {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "tagId required")).build();
        }
        Jsonb jsonb = JsonbBuilder.create();
        Map<?,?> body = null;
        try {
            body = jsonb.fromJson(rawBody, Map.class);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json" , "error", e.getMessage())).build();
        }
        if (body == null || !body.containsKey("tagId")) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "tagId required")).build();
        }
        Long tagId = null;
        Object v = body.get("tagId");
        if (v instanceof Number) tagId = ((Number) v).longValue();
        else tagId = Long.parseLong(v.toString());
        publicationBean.addTag(id, tagId);
        var publication = publicationBean.findWithDetails(id);
        return Response.ok(PublicationDTO.fromWithDetails(publication)).build();
    }
    
    @DELETE
    @Path("/{id}/tags/{tagId}")
    public Response removeTag(@PathParam("id") Long id, @PathParam("tagId") Long tagId) throws Exception {
        publicationBean.removeTag(id, tagId);
        return Response.ok(Map.of("message", "Tag removida da publicação com sucesso")).build();
    }

    @GET
    @Path("/{id}/file")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response downloadFile(@PathParam("id") Long id) throws Exception {
        var document = documentBean.findByPublication(id);
        java.nio.file.Path path = Paths.get(document.getFilepath());
        if (!Files.exists(path)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        StreamingOutput stream = output -> {
            try (InputStream in = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
            }
        };

        return Response.ok(stream)
                       .header("Content-Disposition", "attachment; filename=\"" + document.getFilename() + "\"")
                       .build();
    }
    
    @PATCH
    @Path("/{id}/visibility")
    public Response setVisibility(@PathParam("id") Long id, PublicationDTO dto) throws Exception {
        publicationBean.setVisibility(id, dto.isVisible());
        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }

    // Spec typo compatibility: accept 'visiblity' path as well (PATCH may be unsupported by some servers)
    @POST
    @Path("/{id}/visiblity")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response setVisibilitySpec(@PathParam("id") Long id, String rawBody) throws Exception {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "visible required")).build();
        }
        Jsonb jsonb = JsonbBuilder.create();
        Map<?,?> body = null;
        try {
            body = jsonb.fromJson(rawBody, Map.class);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json", "error", e.getMessage())).build();
        }
        if (body == null || !body.containsKey("visible")) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "visible required")).build();
        }
        boolean visible = false;
        Object v = body.get("visible");
        if (v instanceof Boolean) visible = (Boolean) v;
        else visible = Boolean.parseBoolean(v.toString());
        publicationBean.setVisibility(id, visible);
        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }
}