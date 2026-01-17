package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.SecurityContext;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CollaboratorBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.DocumentBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.PublicationBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ActivityLogBean;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ActivityLogDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;
import pt.ipleiria.dei.ei.estg.researchcenter.security.RequireOwnership;

@Path("publications")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class PublicationService {
    
    @EJB
    private PublicationBean publicationBean;
    @EJB
    private DocumentBean documentBean;
    @EJB
    private CollaboratorBean collaboratorBean;
    @EJB
    private ActivityLogBean activityLogBean;
    @EJB
    private UserBean userBean;
    @Context
    private SecurityContext securityContext;

    private void ensureAuthorOrResponsibleOrAdmin(Long publicationId) throws Exception {
        var pub = publicationBean.find(publicationId);
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        var coll = collaboratorBean.findByUsername(username);
        if (pub.getUploadedBy() != null && pub.getUploadedBy().getId().equals(coll.getId())) return;
        if (securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR")) return;
        throw new ForbiddenException("Insufficient permissions");
    }
    
    @GET
    public Response getAll(
            @QueryParam("search") String search,
            @QueryParam("areaScientific") String areaScientific,
            @QueryParam("tag") Long tagId,
            @QueryParam("dateFrom") String dateFromStr,
            @QueryParam("dateTo") String dateToStr,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        java.time.LocalDateTime dateFrom = null;
        java.time.LocalDateTime dateTo = null;
        try {
            if (dateFromStr != null && !dateFromStr.isBlank()) {
                var odt = java.time.OffsetDateTime.parse(dateFromStr);
                dateFrom = odt.withOffsetSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime();
            }
            if (dateToStr != null && !dateToStr.isBlank()) {
                var odt2 = java.time.OffsetDateTime.parse(dateToStr);
                dateTo = odt2.withOffsetSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime();
            }
        } catch (Exception ex) {
            // ignore parse errors and treat as null
        }

        var pubs = publicationBean.findWithFilters(search, areaScientific, tagId, dateFrom, dateTo, page, size);
        long total = publicationBean.countWithFilters(search, areaScientific, tagId, dateFrom, dateTo);
        int totalPages = size > 0 ? (int) ((total + size - 1) / size) : 1;
        var content = PublicationDTO.from(pubs);
        var result = Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", totalPages,
                "currentPage", page,
                "pageSize", size
        );
        return Response.ok(result).build();
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

    @GET
    @Path("/my-publications")
    @Authenticated
    public Response getMyPublications(@QueryParam("page") @DefaultValue("0") int page,
                                      @QueryParam("size") @DefaultValue("10") int size) throws Exception {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        Long collaboratorId = null;
        try {
            var coll = collaboratorBean.findByUsername(username);
            collaboratorId = coll.getId();
        } catch (MyEntityNotFoundException ex) {
            // If admin without collaborator, return empty page
            if (securityContext.isUserInRole("ADMINISTRADOR")) {
                var empty = List.<Publication>of();
                var content = PublicationDTO.from(empty);
                var result = Map.of(
                        "content", content,
                        "totalElements", 0,
                        "totalPages", 0,
                        "currentPage", page,
                        "pageSize", size
                );
                return Response.ok(result).build();
            } else {
                throw ex;
            }
        }

        var pubs = publicationBean.findByUploadedBy(collaboratorId);
        // Ensure lazy collections are initialized by loading details for each publication
        var detailed = new java.util.ArrayList<Publication>();
        for (var p : pubs) {
            detailed.add(publicationBean.findWithDetails(p.getId()));
        }
        int total = detailed != null ? detailed.size() : 0;
        List<Publication> pageList;
        if (total == 0) pageList = List.of();
        else if (size <= 0) pageList = detailed;
        else {
            int from = Math.max(0, page * size);
            int to = Math.min(total, from + size);
            pageList = from >= to ? List.of() : detailed.subList(from, to);
        }

        var content = PublicationDTO.from(pageList);
        int totalPages = size > 0 ? (int) ((total + size - 1) / size) : 1;
        var result = Map.of(
                "content", content,
                "totalElements", total,
                "totalPages", totalPages,
                "currentPage", page,
                "pageSize", size
        );
        return Response.ok(result).build();
    }
    
    @POST
    @Authenticated
    public Response create(PublicationDTO dto) throws Exception {
        // Use authenticated user as uploader
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        Long uploaderId = null;
        try {
            var uploader = collaboratorBean.findByUsername(username);
            uploaderId = uploader.getId();
        } catch (MyEntityNotFoundException ex) {
            if (securityContext.isUserInRole("ADMINISTRADOR")) {
                // Do not attempt to create a Collaborator with the same username (single-table users).
                // Use the system collaborator as fallback if present, otherwise leave null.
                try {
                    var sys = collaboratorBean.findByUsername("system");
                    uploaderId = sys.getId();
                } catch (MyEntityNotFoundException e) {
                    uploaderId = null;
                }
            } else {
                throw ex;
            }
        }

        var publication = publicationBean.create(
            dto.getTitle(),
            dto.getAuthors(),
            dto.getType(),
            dto.getAreaScientific(),
            dto.getYear(),
            dto.getAbstract_(),
            uploaderId
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

        // Attach tags if provided in metadata
        if (dto.getTags() != null) {
            for (TagDTO t : dto.getTags()) {
                if (t != null && t.getId() != null) publicationBean.addTag(publication.getId(), t.getId());
            }
        }
        var pub = publicationBean.findWithDetails(publication.getId());
        return Response.status(Response.Status.CREATED)
                       .entity(PublicationDTO.fromWithDetails(pub))
                       .build();
    }

    @POST
    @Authenticated
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

        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        Long uploaderId = null;
        try {
            var uploader = collaboratorBean.findByUsername(username);
            uploaderId = uploader.getId();
        } catch (MyEntityNotFoundException ex) {
            if (securityContext.isUserInRole("ADMINISTRADOR")) {
                try {
                    var sys = collaboratorBean.findByUsername("system");
                    uploaderId = sys.getId();
                } catch (MyEntityNotFoundException e) {
                    uploaderId = null;
                }
            } else {
                throw ex;
            }
        }

        var publication = publicationBean.create(
            dto.getTitle(),
            dto.getAuthors(),
            dto.getType(),
            dto.getAreaScientific(),
            dto.getYear(),
            dto.getAbstract_(),
            uploaderId
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

        // Attach tags if provided
        if (dto.getTags() != null) {
            for (TagDTO t : dto.getTags()) {
                if (t != null && t.getId() != null) publicationBean.addTag(publication.getId(), t.getId());
            }
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
    @Authenticated
    @RequireOwnership(parameterName = "id", bypassRoles = {"RESPONSAVEL","ADMINISTRADOR"})
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
    @Authenticated
    @RequireOwnership(parameterName = "id", bypassRoles = {"RESPONSAVEL","ADMINISTRADOR"})
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws Exception {
        
        publicationBean.delete(id);
        return Response.ok(Map.of("message", "Publicação removida com sucesso")).build();
    }
    
    @POST
    @Path("/{id}/tags/{tagId}")
    @Authenticated
    public Response addTag(@PathParam("id") Long id, @PathParam("tagId") Long tagId) throws Exception {
        publicationBean.addTag(id, tagId);
        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }

    // Spec-compliant: accept JSON body { "tagId": 5 }
    @POST
    @Path("/{id}/tags")
    @Authenticated
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
    @Authenticated
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
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
    @Authenticated
    @RequireOwnership(parameterName = "id", bypassRoles = {"RESPONSAVEL","ADMINISTRADOR"})
    @Path("/{id}/visibility")
    public Response setVisibility(@PathParam("id") Long id, PublicationDTO dto) throws Exception {
        publicationBean.setVisibility(id, dto.isVisible());
        var pub = publicationBean.find(id);
        var updatedAt = pub.getUpdatedAt() != null ? pub.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null;
        var res = Map.of(
            "id", pub.getId(),
            "title", pub.getTitle(),
            "visible", pub.isVisible(),
            "updatedAt", updatedAt
        );
        return Response.ok(res).build();
    }

    // Spec typo compatibility: accept 'visiblity' path as well (PATCH may be unsupported by some servers)
    @POST
    @Authenticated
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
        var pub = publicationBean.find(id);
        var updatedAt = pub.getUpdatedAt() != null ? pub.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null;
        var res = Map.of(
            "id", pub.getId(),
            "title", pub.getTitle(),
            "visible", pub.isVisible(),
            "updatedAt", updatedAt
        );
        return Response.ok(res).build();
    }

    @GET
    @Path("/{id}/history")
    @Authenticated
    public Response getPublicationHistory(@PathParam("id") Long id) throws Exception {
        var logs = activityLogBean.getPublicationHistory(id);
        return Response.ok(ActivityLogDTO.from(logs)).build();
    }

    @EJB
    private pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CommentBean commentBean;

    @POST
    @Path("/{id}/comments")
    @Authenticated
    public Response addComment(@PathParam("id") Long id, String rawBody) throws Exception {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "content required"))
                    .build();
        }

        Jsonb jsonb = JsonbBuilder.create();
        Map<?, ?> body;
        try {
            body = jsonb.fromJson(rawBody, Map.class);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "invalid json", "error", e.getMessage()))
                    .build();
        }

        if (body == null || !body.containsKey("content") || body.get("content") == null
                || body.get("content").toString().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "content required"))
                    .build();
        }

        String content = body.get("content").toString();
        String username = securityContext.getUserPrincipal().getName();
        var author = collaboratorBean.findByUsername(username);

        var comment = commentBean.create(content, author.getId(), id);
        return Response.status(Response.Status.CREATED)
                .entity(pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO.from(comment))
                .build();
    }

    @GET
    @Path("/{id}/comments")
    public Response getComments(@PathParam("id") Long id) throws Exception {
        var comments = commentBean.findByPublication(id);
        return Response.ok(pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO.from(comments)).build();
    }
}