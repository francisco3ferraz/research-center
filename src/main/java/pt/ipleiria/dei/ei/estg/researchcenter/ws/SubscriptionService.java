package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CollaboratorBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.TagBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Tag;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.List;
import java.util.Map;

@Path("subscriptions/tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class SubscriptionService {

    @EJB
    private CollaboratorBean collaboratorBean;

    @EJB
    private TagBean tagBean;

    @Context
    private SecurityContext securityContext;

    private static final Logger LOGGER = Logger.getLogger(SubscriptionService.class.getName());

    // EP31 - Subscribe to Tag - POST /api/subscriptions/tags
    @POST
    public Response subscribe(String rawBody) throws MyEntityNotFoundException {
        if (rawBody == null || rawBody.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "tagId required")).build();
        }

        Jsonb jsonb = JsonbBuilder.create();
        try {
            Map<?,?> body;
            try {
                body = jsonb.fromJson(rawBody, Map.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid json", "error", e.getMessage())).build();
            }
            if (body == null || (!body.containsKey("tagId") && !body.containsKey("id"))) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "tagId required")).build();
            }
            Object tagObj = body.containsKey("tagId") ? body.get("tagId") : body.get("id");
            if (tagObj == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "tagId required")).build();
            }
            long tagId;
            if (tagObj instanceof Number) tagId = ((Number) tagObj).longValue();
            else {
                try { tagId = Long.parseLong(tagObj.toString()); } catch (Exception ex) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("message", "invalid tagId")).build();
                }
            }

            String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
            if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");

            LOGGER.info(() -> "Subscribe request principal=" + username + " tagId=" + tagId);
            var coll = collaboratorBean.findByPrincipal(username);
            collaboratorBean.subscribeToTag(coll.getId(), tagId);
            LOGGER.info(() -> "Subscribe persisted for collaboratorId=" + coll.getId() + " tagId=" + tagId);
            Tag tag = tagBean.find(tagId);
            return Response.status(Response.Status.CREATED).entity(TagDTO.from(tag)).build();
        } finally {
            try { jsonb.close(); } catch (Exception ignored) {}
        }
    }

    // EP32 - Unsubscribe from Tag - DELETE /api/subscriptions/tags/{tagId}
    @DELETE
    @Path("/{tagId}")
    public Response unsubscribe(@PathParam("tagId") Long tagId) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");

        LOGGER.info(() -> "Unsubscribe request principal=" + username + " tagId=" + tagId);
        var coll = collaboratorBean.findByPrincipal(username);
        collaboratorBean.unsubscribeFromTag(coll.getId(), tagId);
        LOGGER.info(() -> "Unsubscribe persisted for collaboratorId=" + coll.getId() + " tagId=" + tagId);
        return Response.ok(Map.of("message", "Unsubscribed from tag successfully")).build();
    }

    // EP33 - List Subscriptions - GET /api/subscriptions/tags
    @GET
    public Response listSubscriptions() throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");

        Collaborator coll;
        try {
            LOGGER.info(() -> "List subscriptions request principal=" + username);
            coll = collaboratorBean.findByPrincipal(username);
            List<Tag> tags = collaboratorBean.getSubscribedTagsWithPubs(coll.getId());
            LOGGER.info(() -> "List subscriptions found " + tags.size() + " tags for collaboratorId=" + coll.getId());
            return Response.ok(TagDTO.from(tags)).build();
        } catch (MyEntityNotFoundException ex) {
            // If admin without collaborator record, return empty list
            if (securityContext.isUserInRole("ADMINISTRADOR")) {
                return Response.ok(List.of()).build();
            } else {
                throw ex;
            }
        } catch (Exception e) {
            // Defensive: return a JSON error to help debugging and avoid raw 500 stack traces
            try { e.printStackTrace(); } catch (Exception ignored) {}
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "internal server error", "error", e.getMessage()))
                    .build();
        }
    }

    // Handle CORS preflight / options requests
    @OPTIONS
    public Response options() {
        return Response.ok()
                .header("Allow", "GET, POST, DELETE, OPTIONS")
                .build();
    }
}
