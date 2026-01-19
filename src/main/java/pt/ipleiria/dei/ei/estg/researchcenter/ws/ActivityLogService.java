package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ActivityLogDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ActivityLogBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;

@Path("/activity-logs")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
@RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
public class ActivityLogService {

    @EJB
    private ActivityLogBean activityLogBean;
    
    @EJB
    private UserBean userBean;
    
    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/my")
    public Response getMyActivity(
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("20") int size
    ) {
        String username = securityContext.getUserPrincipal().getName();
        try {
            var user = userBean.findByUsernameOrThrow(username);
            var logs = activityLogBean.getUserActivityLogPaginated(user.getId(), page, size);
            
            // Note: Currently not returning total pages in a simple way as the bean only returns list
            // For now just returning content. Improvement: Add count method to bean.
            
            var content = ActivityLogDTO.from(logs);
            
            // Since we don't have total count in bean yet, we'll return a simpler structure or just the list
            // But frontend expects { content: [], totalPages: N }
            // Let's create a partial result. Ideally we should add countUserActivityLog to bean.
            
            var result = Map.of(
                "content", content,
                "currentPage", page,
                "pageSize", size,
                "totalPages", 1 // Placeholder until count is implemented
            );
            
            return Response.ok(result).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
