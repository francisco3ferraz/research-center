package pt.ipleiria.dei.ei.estg.researchcenter.providers;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
@Produces("application/json")
public class JacksonConfig implements ContextResolver<Object> {
    @Override
    public Object getContext(Class<?> type) {
        // No custom Jackson ObjectMapper provided; let the runtime use JSON-B
        return null;
    }
}
