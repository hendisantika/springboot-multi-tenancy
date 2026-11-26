package com.hendisantika.springbootmultitenancy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 27/11/26
 * Time: 04.47
 */
@ConfigurationProperties(prefix = "multitenancy")
public record MultiTenancyProperties(Map<String, TenantDatabase> tenants) {

    public MultiTenancyProperties {
        tenants = tenants == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(tenants));
    }

    public Set<String> tenantNames() {
        return tenants.keySet();
    }

    /**
     * Connection settings of the dedicated database of a tenant.
     */
    public record TenantDatabase(String url, String username, String password) {
    }
}
