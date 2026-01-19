package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.StatisticsBean;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

@Path("statistics")
@Produces({MediaType.APPLICATION_JSON})
@Authenticated
public class StatisticsService {

    @EJB
    private StatisticsBean statisticsBean;

    @Context
    private SecurityContext securityContext;

    /**
     * EP43 - Overview stats
     * GET /api/statistics/overview
     */
    @GET
    @Path("/overview")
    public Response overview() {
        return Response.ok(statisticsBean.getOverview()).build();
    }

    /**
     * EP44 - Personal stats
     * GET /api/statistics/personal
     */
    @GET
    @Path("/personal")
    public Response personal() {
        String username = securityContext.getUserPrincipal().getName();
        return Response.ok(statisticsBean.getPersonal(username)).build();
    }

    /**
     * EP45 - Top publications
     * GET /api/statistics/top-publications?criteria=rating|comments|views&limit=10
     */
    @GET
    @Path("/top-publications")
    public Response topPublications(@QueryParam("criteria") @DefaultValue("rating") String criteria,
                                    @QueryParam("limit") @DefaultValue("10") int limit) {
        return Response.ok(statisticsBean.getTopPublications(criteria, limit)).build();
    }
}

