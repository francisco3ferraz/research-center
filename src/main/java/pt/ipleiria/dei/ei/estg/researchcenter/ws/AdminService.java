package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.PublicationDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.TagDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.CommentDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.UserSummaryDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.CommentBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.PublicationBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.TagBean;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;
import java.util.List;

@Path("admin")
@Produces({MediaType.APPLICATION_JSON})
@Authenticated
public class AdminService {

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private TagBean tagBean;

    /**
     * EP49 - List hidden publications
     * GET /api/admin/hidden-publications
     */
    @GET
    @Path("/hidden-publications")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response hiddenPublications() {
        var pubs = publicationBean.findAllHidden();
        return Response.ok(PublicationDTO.from(pubs)).build();
    }

    /**
     * EP50 - List hidden comments
     * GET /api/admin/hidden-comments
     */
    @GET
    @Path("/hidden-comments")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response hiddenComments() {
        var comments = commentBean.findAllHidden();
        var result = comments.stream().map(c -> Map.of(
                "id", c.getId(),
                "publicationId", c.getPublication() != null ? c.getPublication().getId() : null,
                "publicationTitle", c.getPublication() != null ? c.getPublication().getTitle() : null,
                "author", c.getAuthor() != null ? Map.of("id", c.getAuthor().getId(), "name", c.getAuthor().getName()) : null,
                "content", c.getText(),
                "visible", c.isVisible(),
                "hiddenBy", c.getHiddenBy() != null ? Map.of("id", c.getHiddenBy().getId(), "name", c.getHiddenBy().getName()) : null,
                "hiddenAt", c.getHiddenAt() != null ? c.getHiddenAt().atOffset(java.time.ZoneOffset.UTC).toString() : null,
                "createdAt", c.getCreatedAt() != null ? c.getCreatedAt().atOffset(java.time.ZoneOffset.UTC).toString() : null
        )).toList();
        return Response.ok(result).build();
    }

    /**
     * EP51 - List hidden tags
     * GET /api/admin/hidden-tags
     */
    @GET
    @Path("/hidden-tags")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response hiddenTags() {
        var tags = tagBean.findAllHidden();
        return Response.ok(TagDTO.from(tags)).build();
    }
}

