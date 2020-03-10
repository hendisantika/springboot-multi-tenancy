package com.hendisantika.springbootmultitenancy.web.controller;

import com.hendisantika.springbootmultitenancy.model.Customer;
import com.hendisantika.springbootmultitenancy.repository.CustomerRepository;
import com.hendisantika.springbootmultitenancy.web.converter.Converters;
import com.hendisantika.springbootmultitenancy.web.dto.CustomerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @Autowired
    private CustomerRepository repository;

    @GET
    @Produces("application/json")
    public List<CustomerDto> getAll() {
        // Return the DTO List:
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(Converters::convert)
                .collect(Collectors.toList());
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public CustomerDto get(@PathParam("id") long id) {
        Customer customer = repository.findById(id).orElse(null);

        // Return the DTO:
        return Converters.convert(customer);
    }

    @POST
    @Produces("application/json")
    public CustomerDto post(CustomerDto customer) {
        // Convert to the Domain Object:
        Customer source = Converters.convert(customer);

        // Store the Entity:
        Customer result = repository.save(source);

        // Return the DTO:
        return Converters.convert(result);
    }

    @DELETE
    @Path("{id}")
    @Produces("application/json")
    public void delete(@PathParam("id") long id) {
        repository.deleteById(id);
    }
}
