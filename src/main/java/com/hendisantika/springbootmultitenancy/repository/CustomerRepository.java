package com.hendisantika.springbootmultitenancy.repository;

import com.hendisantika.springbootmultitenancy.model.Customer;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/03/20
 * Time: 17.05
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
