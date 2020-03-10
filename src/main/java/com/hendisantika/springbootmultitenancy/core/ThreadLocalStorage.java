package com.hendisantika.springbootmultitenancy.core;

/**
 * Created by IntelliJ IDEA.
 * Project : springboot-multi-tenancy
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/03/20
 * Time: 17.03
 */
public class ThreadLocalStorage {
    private static ThreadLocal<String> tenant = new ThreadLocal<>();

    public static String getTenantName() {
        return tenant.get();
    }

    public static void setTenantName(String tenantName) {
        tenant.set(tenantName);
    }

}
