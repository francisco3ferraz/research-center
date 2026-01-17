package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

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

            var coll = collaboratorBean.findByUsername(username);
            collaboratorBean.subscribeToTag(coll.getId(), tagId);
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

        var coll = collaboratorBean.findByUsername(username);
        collaboratorBean.unsubscribeFromTag(coll.getId(), tagId);
        return Response.ok(Map.of("message", "Unsubscribed from tag successfully")).build();
    }

    // EP33 - List Subscriptions - GET /api/subscriptions/tags
    @GET
    public Response listSubscriptions() throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : null;
        if (username == null) throw new jakarta.ws.rs.NotAuthorizedException("Authentication required");

        Collaborator coll;
        try {
            coll = collaboratorBean.findByUsername(username);
        } catch (MyEntityNotFoundException ex) {
            // If admin without collaborator record, return empty list
            if (securityContext.isUserInRole("ADMINISTRADOR")) {
                return Response.ok(List.of()).build();
            } else {
                throw ex;
            }
        }

        // Initialize lazy collection
        int ignored = coll.getSubscribedTags().size();
        List<Tag> tags = coll.getSubscribedTags();
        return Response.ok(TagDTO.fromSimple(tags)).build();
    }
}
