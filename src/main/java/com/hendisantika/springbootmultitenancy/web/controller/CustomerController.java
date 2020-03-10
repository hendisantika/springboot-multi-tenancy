package com.hendisantika.springbootmultitenancy.web.controller;

import com.hendisantika.springbootmultitenancy.repository.CustomerRepository;
import com.hendisantika.springbootmultitenancy.web.converter.Converters;
import com.hendisantika.springbootmultitenancy.web.dto.CustomerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/03/20
 * Time: 17.11
 */
@Component
@Path("/customers")
public class CustomerController {

    private final CustomerRepository repository;

    @Autowired
    public CustomerResource(CustomerRepository repository) {
        this.repository = repository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CustomerDto> getAll() {
        // Return the DTO List:
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(Converters::convert)
                .collect(Collectors.toList());
    }
}
