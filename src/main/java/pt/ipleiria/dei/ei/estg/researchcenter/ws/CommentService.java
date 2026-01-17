package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CollaboratorBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CommentBean;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;

@Path("comments")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class CommentService {

    @EJB
    private CommentBean commentBean;

    @EJB
    private CollaboratorBean collaboratorBean;

    @Context
    private SecurityContext securityContext;

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, String rawBody)
            throws MyEntityNotFoundException, MyConstraintViolationException {

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
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }

        if (body == null || !body.containsKey("content") || body.get("content") == null
                || body.get("content").toString().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "content required"))
                    .build();
        }

        String content = body.get("content").toString();

        var comment = commentBean.find(id);
        String username = securityContext.getUserPrincipal().getName();

        if (!comment.getAuthor().getUsername().equals(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Só pode editar os seus próprios comentários"))
                    .build();
        }

        commentBean.update(id, content);
        var updatedComment = commentBean.find(id);
        return Response.ok(CommentDTO.from(updatedComment)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws MyEntityNotFoundException {
        var comment = commentBean.find(id);
        String username = securityContext.getUserPrincipal().getName();

        // Allow deletion if owner OR if admin/manager
        boolean isOwner = comment.getAuthor().getUsername().equals(username);
        boolean isAdminOrManager = securityContext.isUserInRole("ADMINISTRADOR")
                || securityContext.isUserInRole("RESPONSAVEL");

        if (!isOwner && !isAdminOrManager) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Só pode remover os seus próprios comentários"))
                    .build();
        }

        commentBean.delete(id);
        return Response.ok(Map.of("message", "Comentário removido com sucesso")).build();
    }

    @PATCH
    @Path("/{id}/visibility")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response setVisibility(@PathParam("id") Long id, String rawBody)
            throws MyEntityNotFoundException {

        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "visible required"))
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
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }

        if (body == null || !body.containsKey("visible")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "visible required"))
                    .build();
        }

        boolean visible;
        Object v = body.get("visible");
        if (v instanceof Boolean) {
            visible = (Boolean) v;
        } else {
            visible = Boolean.parseBoolean(v.toString());
        }

        commentBean.setVisibility(id, visible);
        var comment = commentBean.find(id);

        var updatedAt = comment.getUpdatedAt() != null
                ? comment.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC)
                : null;

        return Response.ok(Map.of(
                "id", comment.getId(),
                "visible", comment.isVisible(),
                "updatedAt", updatedAt != null ? updatedAt.toString() : ""
        )).build();
    }
}

