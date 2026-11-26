package com.hendisantika.springbootmultitenancy.web.filter;

import com.hendisantika.springbootmultitenancy.core.ThreadLocalStorage;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/03/20
 * Time: 17.08
 */
@Provider
public class TenantNameFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String TENANT_HEADER = "X-TenantID";

    private final Set<String> tenantNames;

    public TenantNameFilter(Collection<String> tenantNames) {
        this.tenantNames = Set.copyOf(tenantNames);
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        String tenantName = ctx.getHeaderString(TENANT_HEADER);

        if (tenantName == null || tenantName.isBlank()) {
            ctx.abortWith(badRequest("Missing " + TENANT_HEADER + " header"));
            return;
        }

        if (!tenantNames.contains(tenantName)) {
            ctx.abortWith(badRequest("Unknown tenant: " + tenantName));
            return;
        }

        // Set in the Thread Context of the Request:
        ThreadLocalStorage.setTenantName(tenantName);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Threads are pooled, so never leak the tenant into the next request:
        ThreadLocalStorage.clear();
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", message))
                .build();
    }
}
