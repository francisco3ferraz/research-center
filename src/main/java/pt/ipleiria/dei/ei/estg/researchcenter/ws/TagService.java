package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.TagBean;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;
import java.util.List;

@Path("tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class TagService {

    @EJB
    private TagBean tagBean;

    // EP25 - List all visible tags
    @GET
    public Response getAll() {
        List<Tag> tags = tagBean.findAllVisible();
        return Response.ok(TagDTO.from(tags)).build();
    }

    // EP26 - Create tag (RESPONSAVEL or ADMINISTRADOR)
    @POST
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
    public Response create(String rawBody) throws MyEntityExistsException, MyConstraintViolationException {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "name required")).build();
        }
        Jsonb jsonb = JsonbBuilder.create();
        try {
            Map<?,?> body;
            try {
                body = jsonb.fromJson(rawBody, Map.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json", "error", e.getMessage())).build();
            }
            if (body == null || !body.containsKey("name") || body.get("name") == null || body.get("name").toString().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "name required")).build();
            }
            String name = body.get("name").toString();
            String description = body.containsKey("description") && body.get("description") != null ? body.get("description").toString() : null;

            var tag = tagBean.create(name, description);
            return Response.status(Response.Status.CREATED).entity(TagDTO.from(tag)).build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }
    }

    // EP27 - Update tag
    @PUT
    @Path("/{id}")
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
    public Response update(@PathParam("id") Long id, String rawBody) throws MyEntityNotFoundException, MyConstraintViolationException {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "name required")).build();
        }
        Jsonb jsonb = JsonbBuilder.create();
        try {
            Map<?,?> body;
            try {
                body = jsonb.fromJson(rawBody, Map.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json", "error", e.getMessage())).build();
            }
            if (body == null || !body.containsKey("name") || body.get("name") == null || body.get("name").toString().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "name required")).build();
            }
            String name = body.get("name").toString();
            String description = body.containsKey("description") && body.get("description") != null ? body.get("description").toString() : null;

            tagBean.update(id, name, description);
            var tag = tagBean.find(id);
            return Response.ok(TagDTO.from(tag)).build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }
    }

    // EP28 - Delete tag
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"RESPONSAVEL","ADMINISTRADOR"})
    public Response delete(@PathParam("id") Long id) throws MyEntityNotFoundException {
        tagBean.delete(id);
        return Response.ok(Map.of("message", "Tag removida com sucesso")).build();
    }
}
