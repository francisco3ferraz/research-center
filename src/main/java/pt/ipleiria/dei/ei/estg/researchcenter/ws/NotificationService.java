package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import pt.ipleiria.dei.ei.estg.researchcenter.dtos.NotificationDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CollaboratorBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.NotificationBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.Collaborator;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.List;
import java.util.Map;

@Path("notifications")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class NotificationService {

    @EJB
    private NotificationBean notificationBean;

    @EJB
    private CollaboratorBean collaboratorBean;

    @Context
    private SecurityContext securityContext;

    /**
     * EP34 - Listar Notificações
     * GET /api/notifications
     * Query params: unreadOnly (boolean), page (int), size (int)
     */
    @GET
    public Response getNotifications(
            @QueryParam("unreadOnly") @DefaultValue("false") boolean unreadOnly,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        
        String username = securityContext.getUserPrincipal().getName();
        
        Collaborator collaborator;
        try {
            collaborator = collaboratorBean.findByUsername(username);
        } catch (MyEntityNotFoundException e) {
            // If user is not a collaborator (e.g., admin), return empty list
            return Response.ok(Map.of(
                "content", List.of(),
                "totalElements", 0,
                "unreadCount", 0
            )).build();
        }

        var notifications = notificationBean.getUserNotificationsPaginated(
            collaborator.getId(), unreadOnly, page, size);
        var unreadCount = notificationBean.countUnreadNotifications(collaborator.getId());
        
        // Get total count
        var allNotifications = unreadOnly 
            ? notificationBean.getUnreadNotifications(collaborator.getId())
            : notificationBean.getUserNotifications(collaborator.getId());
        
        return Response.ok(Map.of(
            "content", NotificationDTO.from(notifications),
            "totalElements", allNotifications.size(),
            "unreadCount", unreadCount
        )).build();
    }

    /**
     * EP35 - Marcar Notificação como Lida
     * PATCH /api/notifications/{id}/read
     */
    @PATCH
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") Long id) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        
        // Verify the notification belongs to the current user
        var notification = notificationBean.find(id);
        if (!notification.getUser().getUsername().equals(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Não tem permissão para aceder a esta notificação"))
                    .build();
        }
        
        notificationBean.markAsRead(id);
        notification = notificationBean.find(id);
        
        return Response.ok(Map.of(
            "id", notification.getId(),
            "read", notification.isRead(),
            "readAt", notification.getReadAt() != null ? notification.getReadAt().toString() : null
        )).build();
    }

    /**
     * EP36 - Marcar Todas as Notificações como Lidas
     * PATCH /api/notifications/read-all
     */
    @PATCH
    @Path("/read-all")
    public Response markAllAsRead() {
        String username = securityContext.getUserPrincipal().getName();
        
        Collaborator collaborator;
        try {
            collaborator = collaboratorBean.findByUsername(username);
        } catch (MyEntityNotFoundException e) {
            return Response.ok(Map.of(
                "message", "Todas as notificações foram marcadas como lidas",
                "count", 0
            )).build();
        }
        
        int count = notificationBean.markAllAsRead(collaborator.getId());
        
        return Response.ok(Map.of(
            "message", "Todas as notificações foram marcadas como lidas",
            "count", count
        )).build();
    }

    /**
     * EP37 - Remover Notificação
     * DELETE /api/notifications/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        
        // Verify the notification belongs to the current user
        var notification = notificationBean.find(id);
        if (!notification.getUser().getUsername().equals(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Não tem permissão para remover esta notificação"))
                    .build();
        }
        
        notificationBean.delete(id);
        
        return Response.ok(Map.of("message", "Notificação removida com sucesso")).build();
    }
}
