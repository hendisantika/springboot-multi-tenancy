package com.hendisantika.springbootmultitenancy.route;

import com.hendisantika.springbootmultitenancy.core.ThreadLocalStorage;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/03/20
 * Time: 17.06
 */
public class TenantAwareRoutingSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return ThreadLocalStorage.getTenantName();
    }
}
