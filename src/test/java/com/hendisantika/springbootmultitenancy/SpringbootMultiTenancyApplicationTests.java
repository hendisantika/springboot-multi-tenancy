package com.hendisantika.springbootmultitenancy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringbootMultiTenancyApplicationTests {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("sampledb")
            .withInitScript("init-tenants.sql");

    private final HttpClient client = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void tenants(DynamicPropertyRegistry registry) {
        String tenantTwoUrl = POSTGRES.getJdbcUrl().replace("/sampledb", "/sampledb2");
        registry.add("multitenancy.tenants.TenantOne.url", POSTGRES::getJdbcUrl);
        registry.add("multitenancy.tenants.TenantOne.username", POSTGRES::getUsername);
        registry.add("multitenancy.tenants.TenantOne.password", POSTGRES::getPassword);
        registry.add("multitenancy.tenants.TenantTwo.url", () -> tenantTwoUrl);
        registry.add("multitenancy.tenants.TenantTwo.username", POSTGRES::getUsername);
        registry.add("multitenancy.tenants.TenantTwo.password", POSTGRES::getPassword);
    }

    @Test
    void customersAreIsolatedPerTenant() throws Exception {
        assertThat(post("TenantOne", "{\"firstName\":\"Philipp\",\"lastName\":\"Wagner\"}").statusCode()).isEqualTo(200);
        assertThat(post("TenantOne", "{\"firstName\":\"Max\",\"lastName\":\"Mustermann\"}").statusCode()).isEqualTo(200);
        assertThat(post("TenantTwo", "{\"firstName\":\"Hans\",\"lastName\":\"Wurst\"}").statusCode()).isEqualTo(200);

        String tenantOne = get("TenantOne").body();
        String tenantTwo = get("TenantTwo").body();

        assertThat(tenantOne).contains("Philipp", "Max").doesNotContain("Hans");
        assertThat(tenantTwo).contains("Hans").doesNotContain("Philipp", "Max");
    }

    @Test
    void requestWithoutTenantIsRejected() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(uri()).GET().build(), HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void requestWithUnknownTenantIsRejected() throws Exception {
        assertThat(get("UnknownTenant").statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> get(String tenant) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri())
                .header("X-TenantID", tenant)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String tenant, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri())
                .header("X-TenantID", tenant)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri() {
        return URI.create("http://localhost:" + port + "/customers");
    }
}
