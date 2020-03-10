package com.hendisantika.springbootmultitenancy.web.controller;

import com.hendisantika.springbootmultitenancy.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

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
}
