package pt.ipleiria.dei.ei.estg.researchcenter.providers;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null) {
            origin = "*";
        }

        // Handle OPTIONS requests immediately before endpoint matching
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                Response.ok()
                    .header("Access-Control-Allow-Origin", origin)
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD")
                    .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, If-None-Match, Cache-Control, If-Modified-Since")
                    .header("Access-Control-Max-Age", "3600")
                    .build()
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null) {
            origin = "*";
        }

        // Add CORS headers to all non-OPTIONS responses
        if (!"OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, If-None-Match, Cache-Control, If-Modified-Since");
        }
    }
}