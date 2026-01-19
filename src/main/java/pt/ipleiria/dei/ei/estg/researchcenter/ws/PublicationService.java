package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
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
import jakarta.ws.rs.core.HttpHeaders;
import pt.ipleiria.dei.ei.estg.researchcenter.security.TokenIssuer;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.UserRole;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagSimpleDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Publication;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CollaboratorBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.DocumentBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.PublicationBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ActivityLogBean;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ActivityLogDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationHistoryEntryDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;
import pt.ipleiria.dei.ei.estg.researchcenter.security.RequireOwnership;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.RatingDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.RatingBean;

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
    @EJB
    private RatingBean ratingBean;
    @Context
    private SecurityContext securityContext;
    @Context
    private HttpHeaders headers;

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
    @PermitAll
    public Response getAll(
            @QueryParam("search") String search,
            @QueryParam("areaScientific") String areaScientific,
            @QueryParam("tag") Long tagId,
            @QueryParam("dateFrom") String dateFromStr,
            @QueryParam("dateTo") String dateToStr,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order,
            @QueryParam("showHidden") boolean showHidden,
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


        User user = getUserFromToken();
        boolean isGuest = (user == null);
        boolean canSeeConfidential = !isGuest && (user.getRole() == UserRole.ADMINISTRADOR || user.getRole() == UserRole.RESPONSAVEL);
        boolean canSeeHidden = canSeeConfidential && showHidden;

        var pubs = isGuest
                ? publicationBean.findPublicWithFiltersSorted(search, areaScientific, tagId, dateFrom, dateTo, sortBy, order, page, size)
                : publicationBean.findWithFiltersSorted(search, areaScientific, tagId, dateFrom, dateTo, sortBy, order, page, size, canSeeConfidential, canSeeHidden);
        long total = isGuest
                ? publicationBean.countPublicWithFilters(search, areaScientific, tagId, dateFrom, dateTo)
                : publicationBean.countWithFilters(search, areaScientific, tagId, dateFrom, dateTo, canSeeConfidential, canSeeHidden);
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
    @PermitAll
    public Response get(@PathParam("id") Long id) throws Exception {
        var pub = publicationBean.findWithDetails(id);
        User user = getUserFromToken();
        
        if (user == null) {
            // Guest restrictions
            if (!pub.isVisible() || pub.isConfidential()) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Publicação não disponível para convidados"))
                        .build();
            }
        } else {
            // Authenticated restrictions for hidden/confidential
            if (!pub.isVisible() || pub.isConfidential()) {
                boolean isPermitted = (user.getRole() == UserRole.ADMINISTRADOR || 
                                       user.getRole() == UserRole.RESPONSAVEL ||
                                       (pub.getUploadedBy() != null && pub.getUploadedBy().getId().equals(user.getId())));
                if (!isPermitted) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of("message", "Apenas administradores, responsáveis ou o autor podem ver esta publicação"))
                            .build();
                }
            }
        }

        // Track views for statistics/top-publications
        publicationBean.incrementViews(id);
        return Response.ok(PublicationDTO.fromWithDetails(pub)).build();
    }
    
    // Helper to get User from token manually (for optional auth endpoints like get/getAll)
    private pt.ipleiria.dei.ei.estg.researchcenter.entities.User getUserFromToken() {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7).trim();
            var key = Keys.hmacShaKeyFor(TokenIssuer.SECRET_KEY);
            String username = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
            if (username != null) {
                return userBean.findByUsername(username);
            }
        } catch (Exception ignore) {}
        return null;
    }

    /**
     * EP46 - Advanced search (best-effort; final contract depends on the provided PDF spec)
     * POST /api/publications/advanced-search
     */
    @POST
    @Path("/advanced-search")
    @Authenticated
    public Response advancedSearch(String rawBody,
                                  @QueryParam("page") @DefaultValue("0") int page,
                                  @QueryParam("size") @DefaultValue("20") int size) throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        Map<?, ?> body;
        try {
            body = rawBody != null && !rawBody.isBlank() ? jsonb.fromJson(rawBody, Map.class) : Map.of();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "invalid json", "error", e.getMessage()))
                    .build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }

        List<String> keywords = extractStringList(body, "keywords");
        List<String> authors = extractStringList(body, "authors");
        List<String> typesRaw = extractStringList(body, "types");
        List<pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType> types = new java.util.ArrayList<>();
        if (typesRaw != null) {
            for (String t : typesRaw) {
                try { types.add(pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType.valueOf(t)); } catch (Exception ignore) {}
            }
        }
        List<Long> scientificAreas = extractLongList(body, "scientificAreas");
        List<Long> tags = extractLongList(body, "tags");
        Integer yearFrom = extractInt(body, "yearFrom");
        Integer yearTo = extractInt(body, "yearTo");
        Double minRating = extractDouble(body, "minRating");
        Boolean hasComments = extractBoolean(body, "hasComments");
        Boolean confidential = extractBoolean(body, "confidential");
        // Enforce role restrictions
        if (!securityContext.isUserInRole("ADMINISTRADOR") && !securityContext.isUserInRole("RESPONSAVEL")) {
             confidential = false;
        }
        String sortBy = body != null && body.get("sortBy") != null ? body.get("sortBy").toString() : null;
        String order = body != null && body.get("order") != null ? body.get("order").toString() : null;
        Integer pageBody = extractInt(body, "page");
        Integer sizeBody = extractInt(body, "size");
        int p = pageBody != null ? pageBody : page;
        int s = sizeBody != null ? sizeBody : size;

        var pubs = publicationBean.advancedSearch(keywords, authors, types, scientificAreas, tags, yearFrom, yearTo, minRating, hasComments, confidential, sortBy, order, p, s);
        long total = publicationBean.countAdvancedSearch(keywords, authors, types, scientificAreas, tags, yearFrom, yearTo, minRating, hasComments, confidential);
        int totalPages = s > 0 ? (int) ((total + s - 1) / s) : 1;

        return Response.ok(Map.of(
                "content", PublicationDTO.from(pubs),
                "totalElements", total,
                "totalPages", totalPages,
                "currentPage", p,
                "pageSize", s
        )).build();
    }

    private static List<String> extractStringList(Map<?, ?> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) return null;
        Object v = body.get(key);
        if (v instanceof List) {
            return ((List<?>) v).stream().map(Object::toString).map(String::trim).filter(s -> !s.isBlank()).toList();
        }
        return List.of(v.toString());
    }

    private static List<Long> extractLongList(Map<?, ?> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) return null;
        Object v = body.get(key);
        if (v instanceof List) {
            return ((List<?>) v).stream().map(o -> {
                if (o instanceof Number) return ((Number) o).longValue();
                return Long.parseLong(o.toString());
            }).toList();
        }
        if (v instanceof Number) return List.of(((Number) v).longValue());
        return List.of(Long.parseLong(v.toString()));
    }

    private static Integer extractInt(Map<?, ?> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) return null;
        Object v = body.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private static Double extractDouble(Map<?, ?> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) return null;
        Object v = body.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return null; }
    }

    private static Boolean extractBoolean(Map<?, ?> body, String key) {
        if (body == null || !body.containsKey(key) || body.get(key) == null) return null;
        Object v = body.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }

    /**
     * EP47 - Export publications (best-effort)
     * GET /api/publications/export?format=csv|json + same filters as list
     */
    @GET
    @Path("/export")
    @Authenticated
    @Produces({MediaType.APPLICATION_JSON, "text/csv"})
    public Response exportPublications(@QueryParam("format") @DefaultValue("csv") String format,
                                       @QueryParam("search") String search,
                                       @QueryParam("areaScientific") String areaScientific,
                                       @QueryParam("tag") Long tagId,
                                       @QueryParam("dateFrom") String dateFromStr,
                                       @QueryParam("dateTo") String dateToStr,
                                       @QueryParam("sortBy") String sortBy,
                                       @QueryParam("showHidden") boolean showHidden,
                                       @QueryParam("order") String order) {
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
        } catch (Exception ignore) {}

        // Export all matching (no pagination)
        // Export all matching (no pagination)
        boolean canSeeConfidential = securityContext.isUserInRole("ADMINISTRADOR") || securityContext.isUserInRole("RESPONSAVEL");
        boolean canSeeHidden = canSeeConfidential && showHidden;
        var pubs = publicationBean.findWithFiltersSorted(search, areaScientific, tagId, dateFrom, dateTo, sortBy, order, 0, 0, canSeeConfidential, canSeeHidden);
        var dtos = PublicationDTO.from(pubs);

        if ("json".equalsIgnoreCase(format)) {
            return Response.ok(dtos).build();
        }

        StreamingOutput stream = output -> {
            String header = "id,title,type,areaScientific,year,averageRating,ratingsCount,commentsCount,viewsCount\r\n";
            output.write(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (PublicationDTO p : dtos) {
                String line = String.format("%d,%s,%s,%s,%d,%.2f,%d,%d,%d\r\n",
                        p.getId() != null ? p.getId() : 0,
                        escapeCsv(p.getTitle()),
                        p.getType() != null ? p.getType().name() : "",
                        escapeCsv(p.getAreaScientific()),
                        p.getYear() != null ? p.getYear() : 0,
                        p.getAverageRating(),
                        p.getRatingsCount(),
                        p.getCommentsCount(),
                        p.getViewsCount()
                );
                output.write(line.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        };

        return Response.ok(stream, "text/csv")
                .header("Content-Disposition", "attachment; filename=\"publications.csv\"")
                .build();
    }

    private static String escapeCsv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
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

        // Activity log for publication creation
        try {
            var user = userBean.findByUsername(username);
            if (user != null) {
                activityLogBean.create(user, "CREATE", "PUBLICATION", publication.getId(), "Publicação criada");
            }
        } catch (Exception ignore) {}
        
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
            for (TagSimpleDTO t : dto.getTags()) {
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
        try {
            var user = userBean.findByUsername(username);
            if (user != null) {
                activityLogBean.create(user, "CREATE", "PUBLICATION", publication.getId(), "Publicação criada");
            }
        } catch (Exception ignore) {}

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
            for (TagSimpleDTO t : dto.getTags()) {
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

        // Activity log for update (best-effort: changed fields from non-null inputs)
        try {
            String username = securityContext.getUserPrincipal().getName();
            var user = userBean.findByUsername(username);
            if (user != null) {
                var fields = new java.util.ArrayList<String>();
                if (dto.getTitle() != null) fields.add("title");
                if (dto.getAuthors() != null) fields.add("authors");
                if (dto.getAbstract_() != null) fields.add("abstract");
                if (dto.getAiGeneratedSummary() != null) fields.add("aiGeneratedSummary");
                if (dto.getYear() != null) fields.add("year");
                if (dto.getPublisher() != null) fields.add("publisher");
                if (dto.getDoi() != null) fields.add("doi");
                activityLogBean.createWithChangedFields(
                        user,
                        "UPDATE",
                        "PUBLICATION",
                        id,
                        "Publicação atualizada",
                        String.join(",", fields)
                );
            }
        } catch (Exception ignore) {}

        var resultDto = publicationBean.getDTOWithDetails(id);
        return Response.ok(resultDto).build();
    }
    
    @DELETE
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
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
        return Response.ok(Map.of(
                "publicationId", id,
                "tags", TagSimpleDTO.from(publication.getTags())
        )).build();
    }
    
    @DELETE
    @Authenticated
    @RequireOwnership(parameterName = "id", bypassRoles = {"RESPONSAVEL","ADMINISTRADOR"})
    @Path("/{id}/tags/{tagId}")
    public Response removeTag(@PathParam("id") Long id, @PathParam("tagId") Long tagId) throws Exception {
        publicationBean.removeTag(id, tagId);
        return Response.ok(Map.of("message", "Tag removida da publicação com sucesso")).build();
    }

    @GET
    @Path("/{id}/file")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @Authenticated
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

    // Spec typo compatibility: accept 'visiblity' path as well (PATCH may be unsupported by some servers)
    @POST
    @Authenticated
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
    @Path("/{id}/visibility")
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
        String username = securityContext.getUserPrincipal().getName();
        publicationBean.setVisibility(id, visible, username);
        try {
            var user = userBean.findByUsername(username);
            if (user != null) {
                activityLogBean.createWithChangedFields(
                        user,
                        "UPDATE",
                        "PUBLICATION",
                        id,
                        visible ? "Publicação mostrada" : "Publicação ocultada",
                        "visible"
                );
            }
        } catch (Exception ignore) {}
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

    // Spec path typo: /visiblity (PATCH)
    @PATCH
    @Authenticated
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
    @Path("/{id}/visiblity")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response setVisibilitySpecTypo(@PathParam("id") Long id, String rawBody) throws Exception {
        return setVisibilitySpec(id, rawBody);
    }

    @GET
    @Path("/{id}/history")
    @Authenticated
    public Response getPublicationHistory(@PathParam("id") Long id) throws Exception {
        var logs = activityLogBean.getPublicationHistory(id);
        var mapped = logs.stream().map(PublicationHistoryEntryDTO::from).toList();
        return Response.ok(mapped).build();
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
        
        // Try to find the collaborator - only collaborators can add comments
        pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator author;
        try {
            author = collaboratorBean.findByUsername(username);
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Apenas colaboradores podem adicionar comentários. O utilizador '" + username + "' não é um colaborador."))
                    .build();
        }

        var comment = commentBean.create(content, author.getId(), id);
        return Response.status(Response.Status.CREATED)
                .entity(pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO.from(comment))
                .build();
    }

    @GET
    @Path("/{id}/comments")
    @PermitAll
    public Response getComments(@PathParam("id") Long id) throws Exception {
        User user = getUserFromToken();
        boolean canSeeHidden = (user != null) && (user.getRole() == UserRole.ADMINISTRADOR || user.getRole() == UserRole.RESPONSAVEL);

        var comments = canSeeHidden 
            ? commentBean.findAllByPublicationIncludingHidden(id)
            : commentBean.findByPublication(id);

        return Response.ok(pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO.from(comments)).build();
    }

    // Ratings endpoints
    @POST
    @Path("/{id}/ratings")
    @Authenticated
    public Response addRating(@PathParam("id") Long id, String rawBody) throws Exception {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "value required")).build();
        }

        Jsonb jsonb = JsonbBuilder.create();
        Map<?, ?> body;
        try {
            body = jsonb.fromJson(rawBody, Map.class);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json", "error", e.getMessage())).build();
        }

        if (body == null || !body.containsKey("value")) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "value required")).build();
        }

        int value;
        Object v = body.get("value");
        if (v instanceof Number) value = ((Number) v).intValue();
        else {
            try { value = Integer.parseInt(v.toString()); } catch (Exception e) { return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid value" , "error", e.getMessage())).build(); }
        }

        if (value < 1 || value > 5) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "value must be between 1 and 5")).build();
        }

        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        var author = collaboratorBean.findByUsername(username);

        var rating = ratingBean.upsert(value, author.getId(), id);
        // Spec doesn't differentiate create vs update status; return OK with rating payload
        return Response.ok(RatingDTO.from(rating)).build();
    }

    @GET
    @Path("/{id}/ratings")
    @PermitAll
    public Response getRatings(@PathParam("id") Long id) throws Exception {
        var ratings = ratingBean.findByPublication(id);
        // Spec: wrapper with averageRating, totalRatings, distribution, ratings[]
        double avg = 0.0;
        int total = ratings != null ? ratings.size() : 0;
        if (total > 0) {
            avg = ratings.stream().mapToInt(pt.ipleiria.dei.ei.estg.researchcenter.entities.Rating::getStars).average().orElse(0.0);
        }
        java.util.Map<String, Integer> dist = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(String.valueOf(i), 0);
        if (ratings != null) {
            for (var r : ratings) {
                String k = String.valueOf(r.getStars());
                dist.put(k, dist.getOrDefault(k, 0) + 1);
            }
        }
        return Response.ok(java.util.Map.of(
                "averageRating", avg,
                "totalRatings", total,
                "distribution", dist,
                "ratings", RatingDTO.from(ratings)
        )).build();
    }

    @DELETE
    @Path("/{id}/ratings")
    @Authenticated
    public Response removeRating(@PathParam("id") Long id) throws Exception {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");
        var user = collaboratorBean.findByUsername(username);
        ratingBean.deleteByUserAndPublication(user.getId(), id);
        return Response.ok(Map.of("message", "Rating removido com sucesso")).build();
    }
}