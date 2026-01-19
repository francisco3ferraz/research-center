package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ScientificAreaDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ScientificAreaBean;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;

@Path("scientific-areas")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ScientificAreaService {

    @EJB
    private ScientificAreaBean scientificAreaBean;

    /**
     * EP38 - Listar Áreas Científicas
     * GET /api/scientific-areas
     */
    @GET
    @PermitAll
    public Response getAll() {
        var areas = scientificAreaBean.findAll();
        return Response.ok(ScientificAreaDTO.from(areas)).build();
    }

    /**
     * EP39 - Criar Área Científica
     * POST /api/scientific-areas
     */
    @POST
    @Authenticated
    @RolesAllowed({"ADMINISTRADOR"})
    public Response create(String rawBody) 
            throws MyEntityExistsException, MyConstraintViolationException {
        
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "name required"))
                    .build();
        }

        Jsonb jsonb = JsonbBuilder.create();
        try {
            Map<?, ?> body;
            try {
                body = jsonb.fromJson(rawBody, Map.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "invalid json", "error", e.getMessage()))
                        .build();
            }

            if (body == null || !body.containsKey("name") || body.get("name") == null 
                    || body.get("name").toString().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "name required"))
                        .build();
            }

            String name = body.get("name").toString();
            String description = body.containsKey("description") && body.get("description") != null 
                    ? body.get("description").toString() 
                    : null;

            var area = scientificAreaBean.create(name, description);
            return Response.status(Response.Status.CREATED)
                    .entity(ScientificAreaDTO.from(area))
                    .build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * EP40 - Editar Área Científica
     * PUT /api/scientific-areas/{id}
     */
    @PUT
    @Path("/{id}")
    @Authenticated
    @RolesAllowed({"ADMINISTRADOR"})
    public Response update(@PathParam("id") Long id, String rawBody) 
            throws MyEntityNotFoundException, MyConstraintViolationException, MyEntityExistsException {
        
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "name required"))
                    .build();
        }

        Jsonb jsonb = JsonbBuilder.create();
        try {
            Map<?, ?> body;
            try {
                body = jsonb.fromJson(rawBody, Map.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "invalid json", "error", e.getMessage()))
                        .build();
            }

            if (body == null || !body.containsKey("name") || body.get("name") == null 
                    || body.get("name").toString().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "name required"))
                        .build();
            }

            String name = body.get("name").toString();
            String description = body.containsKey("description") && body.get("description") != null 
                    ? body.get("description").toString() 
                    : null;

            var area = scientificAreaBean.update(id, name, description);
            return Response.ok(ScientificAreaDTO.from(area)).build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * EP41 - Remover Área Científica
     * DELETE /api/scientific-areas/{id}
     */
    @DELETE
    @Path("/{id}")
    @Authenticated
    @RolesAllowed({"ADMINISTRADOR"})
    public Response delete(@PathParam("id") Long id) throws MyEntityNotFoundException {
        scientificAreaBean.delete(id);
        return Response.ok(Map.of("message", "Área científica removida com sucesso")).build();
    }
}
