package br.com.eduardoenemark.rwt.core.config.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppCoreConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger("br.com.eduardoenemark.rwt.core");

    public static final String READ_DATASOURCE = "readDataSource";
    public static final String WRITE_DATASOURCE = "writeDataSource";
    public static final String ROUTING_DATASOURCE = "routingDataSource";

    public static final String READ_HIBERNATE = "readHibernate";
    public static final String WRITE_HIBERNATE = "writeHibernate";

    public static final String READ_ENTITY_MANAGER_FACTORY = "readEntityManagerFactory";
    public static final String WRITE_ENTITY_MANAGER_FACTORY = "writeEntityManagerFactory";

}
