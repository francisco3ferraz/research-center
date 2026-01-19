package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.ActivityLogDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.UserDTO;
import pt.ipleiria.dei.ei.estg.researchcenter.dtos.UserSummaryDTO;
import java.util.List;
import java.util.stream.Collectors;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.ActivityLogBean;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.UserBean;
import pt.ipleiria.dei.ei.estg.researchcenter.entities.UserRole;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyConstraintViolationException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityExistsException;
import pt.ipleiria.dei.ei.estg.researchcenter.exceptions.MyEntityNotFoundException;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;

@Path("users")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class UserService {

    @EJB
    private UserBean userBean;

    @EJB
    private ActivityLogBean activityLogBean;

    @Context
    private SecurityContext securityContext;

    @GET
    @RolesAllowed({"ADMINISTRADOR"})
    public Response getAll() {
        var users = userBean.findAll();
        return Response.ok(UserDTO.from(users)).build();
    }

    @GET
    @Path("/lookup")
    public Response lookup(@QueryParam("q") String q) {
        var users = userBean.searchByName(q);
        var dtos = users.stream().map(UserSummaryDTO::from).collect(Collectors.toList());
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) throws MyEntityNotFoundException {
        var user = userBean.find(id);
        return Response.ok(UserDTO.from(user)).build();
    }

    @POST
    @RolesAllowed({"ADMINISTRADOR"})
    public Response create(UserDTO dto) throws MyEntityExistsException, MyConstraintViolationException {

        // Parse role, default to COLABORADOR
        UserRole role = UserRole.COLABORADOR;
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                role = UserRole.valueOf(dto.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid role: " + dto.getRole()))
                        .build();
            }
        }

        var user = userBean.create(
                dto.getUsername(),
                dto.getPassword(),
                dto.getName(),
                dto.getEmail(),
                role
        );

        return Response.status(Response.Status.CREATED)
                .entity(UserDTO.from(user))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response update(@PathParam("id") Long id, UserDTO dto)
            throws MyEntityNotFoundException, MyConstraintViolationException {

        // Parse role if provided
        UserRole role = null;
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                role = UserRole.valueOf(dto.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid role: " + dto.getRole()))
                        .build();
            }
        }

        var user = userBean.update(id, dto.getName(), dto.getEmail(), role);
        return Response.ok(UserDTO.from(user)).build();
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response setStatus(@PathParam("id") Long id, UserDTO dto) throws MyEntityNotFoundException {
        var user = userBean.setActive(id, dto.isActive());
        return Response.ok(UserDTO.from(user)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response delete(@PathParam("id") Long id) throws MyEntityNotFoundException {
        userBean.delete(id);
        return Response.ok(Map.of("message", "Utilizador removido com sucesso")).build();
    }

    @PUT
    @Path("/profile")
    public Response updateProfile(UserDTO dto) throws MyEntityNotFoundException, MyConstraintViolationException {
        // Get the authenticated user's username
        String username = securityContext.getUserPrincipal().getName();

        var user = userBean.updateProfile(username, dto.getName(), dto.getEmail());
        return Response.ok(UserDTO.from(user)).build();
    }

    @GET
    @Path("/{id}/activity")
    public Response getActivity(@PathParam("id") Long id,
                                @QueryParam("page") @DefaultValue("0") int page,
                                @QueryParam("size") @DefaultValue("20") int size) throws MyEntityNotFoundException {
        // Check if user is viewing their own activity or is an admin
        String username = securityContext.getUserPrincipal().getName();
        var currentUser = userBean.findByUsernameOrThrow(username);

        // Only allow viewing own activity or if admin
        if (!currentUser.getId().equals(id) &&
            !securityContext.isUserInRole("ADMINISTRADOR")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Não tem permissão para ver o histórico deste utilizador"))
                    .build();
        }

        var activities = activityLogBean.getUserActivityLogPaginated(id, page, size);
        return Response.ok(ActivityLogDTO.from(activities)).build();
    }
}
