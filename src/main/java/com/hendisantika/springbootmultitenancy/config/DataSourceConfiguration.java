package com.hendisantika.springbootmultitenancy.config;

import com.hendisantika.springbootmultitenancy.route.TenantAwareRoutingSource;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

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
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MultiTenancyProperties.class)
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource(MultiTenancyProperties properties) {
        if (properties.tenants().isEmpty()) {
            throw new IllegalStateException("No tenants configured. Please define 'multitenancy.tenants.<TenantName>.url|username|password'.");
        }

        Map<Object, Object> targetDataSources = new LinkedHashMap<>();
        properties.tenants().forEach((name, database) -> {
            DataSource tenantDataSource = createDataSource(name, database);
            migrate(tenantDataSource);
            targetDataSources.put(name, tenantDataSource);
        });

        TenantAwareRoutingSource routingSource = new TenantAwareRoutingSource();
        routingSource.setTargetDataSources(targetDataSources);
        // Only used while bootstrapping (no tenant bound to the current thread), e.g. by Hibernate:
        routingSource.setDefaultTargetDataSource(targetDataSources.values().iterator().next());
        // Never silently route an unknown tenant to the default database:
        routingSource.setLenientFallback(false);
        routingSource.afterPropertiesSet();
        return routingSource;
    }

    private static DataSource createDataSource(String name, MultiTenancyProperties.TenantDatabase database) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("tenant-" + name);
        dataSource.setJdbcUrl(database.url());
        dataSource.setUsername(database.username());
        dataSource.setPassword(database.password());
        return dataSource;
    }

    private static void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
