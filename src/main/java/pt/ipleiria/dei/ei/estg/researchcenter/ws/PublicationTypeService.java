package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;

import pt.ipleiria.dei.ei.estg.researchcenter.entities.PublicationType;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("publication-types")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class PublicationTypeService {

    /**
     * EP42 - List publication types
     * GET /api/publication-types
     */
    @GET
    @PermitAll
    public Response getAll() {
        PublicationType[] types = PublicationType.values();
        List<Map<String, String>> list = new ArrayList<>();
        for (PublicationType t : types) {
            list.add(Map.of(
                    "code", t.name(),
                    "name", t.getName(),
                    "description", t.getDescription()
            ));
        }
        return Response.ok(list).build();
    }
}

