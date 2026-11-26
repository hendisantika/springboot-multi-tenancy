# springboot-multi-tenancy

[![Java CI with Maven](https://github.com/hendisantika/springboot-multi-tenancy/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/springboot-multi-tenancy/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.2.0-brightgreen?logo=springboot)
![Dependabot](https://img.shields.io/badge/Dependabot-enabled-blue?logo=dependabot)

## Providing Multitenancy with Spring Boot

This project is an example of **database-per-tenant** multi tenancy with Spring Boot, based on:

* https://bytefish.de/blog/spring_boot_multitenancy/

Every request carries an `X-TenantID` header. A Jersey filter binds the tenant to the current thread, and an
`AbstractRoutingDataSource` routes all JDBC/JPA calls to that tenant's own PostgreSQL database.
Requests without a tenant, or with an unknown tenant, are rejected with `400 Bad Request`.

## Tech Stack

* Java 25
* Spring Boot 4.2 (Jersey / JAX-RS, Spring Data JPA, Hibernate 7)
* PostgreSQL + Flyway (migrations are applied to every tenant database on startup)
* HikariCP (one connection pool per tenant)
* Lombok
* JUnit 5 + Testcontainers (integration tests)
* GitHub Actions CI + Dependabot

## How It Works

| Component                  | Responsibility                                                                 |
|----------------------------|--------------------------------------------------------------------------------|
| `MultiTenancyProperties`   | Binds `multitenancy.tenants.<TenantName>.*` from configuration                 |
| `DataSourceConfiguration`  | Creates a Hikari pool per tenant, runs Flyway and builds the routing source    |
| `TenantAwareRoutingSource` | Picks the target `DataSource` using the tenant stored in `ThreadLocalStorage`  |
| `TenantNameFilter`         | Validates `X-TenantID`, binds it to the thread and clears it after the request |
| `CustomerController`       | JAX-RS resource exposing `/customers`                                          |

## Prerequisites

* JDK 25
* Docker (for PostgreSQL and for running the tests with Testcontainers)
* Maven is optional, the Maven Wrapper (`./mvnw`) is included

## Configuration

Tenants are configured in `src/main/resources/application.properties`. The tenant name is the value clients send in
the `X-TenantID` header:

```properties
multitenancy.tenants.TenantOne.url=jdbc:postgresql://localhost:5432/sampledb
multitenancy.tenants.TenantOne.username=philipp
multitenancy.tenants.TenantOne.password=test_pwd

multitenancy.tenants.TenantTwo.url=jdbc:postgresql://localhost:5432/sampledb2
multitenancy.tenants.TenantTwo.username=philipp
multitenancy.tenants.TenantTwo.password=test_pwd
```

Add a tenant by adding another `multitenancy.tenants.<TenantName>.*` block. Values can be overridden with environment
variables or command-line arguments, e.g. `--multitenancy.tenants.TenantOne.password=secret`.

## Running Locally

1. Start PostgreSQL and create both tenant databases:

   ```shell
   docker run -d --name multitenancy-db -p 5432:5432 \
     -e POSTGRES_USER=philipp -e POSTGRES_PASSWORD=test_pwd -e POSTGRES_DB=sampledb \
     postgres:18-alpine

   docker exec multitenancy-db psql -U philipp -d sampledb -c "CREATE DATABASE sampledb2;"
   ```

2. Run the application (the schema is created by Flyway on startup):

   ```shell
   ./mvnw spring-boot:run
   ```

## Running the Tests

The integration tests start a PostgreSQL container with Testcontainers, so Docker must be running:

```shell
./mvnw verify
```

## CI & Dependency Updates

* **GitHub Actions** (`.github/workflows/maven.yml`) builds and tests the project on JDK 25 for every push and pull
  request to `master`, then submits the dependency graph.
* **Dependabot** (`.github/dependabot.yml`) checks Maven and GitHub Actions dependencies daily, groups related updates
  (Spring Boot, database, testing, Maven plugins, GitHub Actions) and uses conventional commit prefixes
  (`build(deps)`, `ci(deps)`).

## Example

We start with inserting customers to the database of Tenant ``TenantOne``:

```
> curl -H "X-TenantID: TenantOne" -H "Content-Type: application/json" -X POST -d "{\"firstName\" : \"Philipp\", \"lastName\" : \"Wagner\"}"  http://localhost:8080/customers

{"id":1,"firstName":"Philipp","lastName":"Wagner"}

> curl -H "X-TenantID: TenantOne" -H "Content-Type: application/json" -X POST -d "{\"firstName\" : \"Max\", \"lastName\" : \"Mustermann\"}"  http://localhost:8080/customers

{"id":2,"firstName":"Max","lastName":"Mustermann"}
```

Getting a list of all customers for ``TenantOne`` will now return two customers:

```
> curl -H "X-TenantID: TenantOne" -X GET http://localhost:8080/customers

[{"id":1,"firstName":"Philipp","lastName":"Wagner"},{"id":2,"firstName":"Max","lastName":"Mustermann"}]
```

While requesting a list of all customers for ``TenantTwo`` returns an empty list:

```
> curl -H "X-TenantID: TenantTwo" -X GET http://localhost:8080/customers

[]
```

We can now insert a customer into the ``TenantTwo`` database:

```
> curl -H "X-TenantID: TenantTwo" -H "Content-Type: application/json" -X POST -d "{\"firstName\" : \"Hans\", \"lastName\" : \"Wurst\"}"  http://localhost:8080/customers

{"id":1,"firstName":"Hans","lastName":"Wurst"}
```

Querying the ``TenantOne`` database still returns the two customers:

```
> curl -H "X-TenantID: TenantOne" -X GET http://localhost:8080/customers

[{"id":1,"firstName":"Philipp","lastName":"Wagner"},{"id":2,"firstName":"Max","lastName":"Mustermann"}]
```

Querying the ``TenantTwo`` database will now return the inserted customer:

```
> curl -H "X-TenantID: TenantTwo" -X GET http://localhost:8080/customers

[{"id":1,"firstName":"Hans","lastName":"Wurst"}]
```

Requests without a tenant are rejected:

```
> curl -i -X GET http://localhost:8080/customers

HTTP/1.1 400
{"error":"Missing X-TenantID header"}
```
