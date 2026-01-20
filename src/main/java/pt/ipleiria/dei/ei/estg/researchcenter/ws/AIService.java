package pt.ipleiria.dei.ei.estg.researchcenter.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.dei.ei.estg.researchcenter.ejbs.LLMClientBean;
import pt.ipleiria.dei.ei.estg.researchcenter.security.Authenticated;

import java.util.Map;
import java.util.logging.Logger;

@Path("ai")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class AIService {
    
    private static final Logger logger = Logger.getLogger(AIService.class.getName());
    
    @EJB
    private LLMClientBean llmClientBean;
    
    @POST
    @Path("/generate-summary")
    public Response generateSummary(Map<String, String> request) {
        String title = request.get("title");
        String abstractContent = request.get("abstract");
        
        // Validate input
        if (title == null || title.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Title is required"))
                    .build();
        }
        
        try {
            // Check if Ollama service is available
            if (!llmClientBean.isServiceAvailable()) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(Map.of("message", "AI service is not available. Please ensure Ollama is running."))
                        .build();
            }
            
            // Generate summary
            String summary = llmClientBean.generateSummary(title, abstractContent);
            
            return Response.ok(Map.of("summary", summary)).build();
            
        } catch (Exception e) {
            logger.severe("Error generating summary: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Failed to generate summary: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/status")
    public Response checkStatus() {
        boolean available = llmClientBean.isServiceAvailable();
        return Response.ok(Map.of("available", available)).build();
    }
}
