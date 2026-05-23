//package br.com.eduardoenemark.rwt.app.server.config;
//
//import br.com.eduardoenemark.rwt.app.server.entity.Product;
//import br.com.eduardoenemark.rwt.core.config.routing.RoutingDataSource;
//import br.com.eduardoenemark.rwt.core.operation.OperationType;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import javax.sql.DataSource;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Properties;
//
///**
// * Test replacement for the full Atomikos JTA datasource setup.
// * Uses H2 in-memory with a single DataSource for both read and write.
// */
//@TestConfiguration
//@Profile("test")
//@PropertySource("classpath:application-test.properties")
//@EnableTransactionManagement
//public class TestDataSourceConfiguration {
//
//    @Primary
//    @Bean(name = "routingDataSource")
//    public DataSource routingDataSource() {
//        HikariDataSource ds = new HikariDataSource();
//        ds.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
//        ds.setDriverClassName("org.h2.Driver");
//        ds.setUsername("sa");
//        ds.setPassword("");
//
//        RoutingDataSource routingDs = new RoutingDataSource();
//        routingDs.setTargetDataSources(
//                Collections.unmodifiableMap(
//                        new HashMap<OperationType, DataSource>() {{
//                            put(OperationType.READ, ds);
//                            put(OperationType.WRITE, ds);
//                        }}));
//        routingDs.setDefaultTargetDataSource(ds);
//        return routingDs;
//    }
//
//    @Primary
//    @Bean(name = "readEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//            @Qualifier("routingDataSource") DataSource dataSource) {
//        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//        em.setDataSource(dataSource);
//        em.setPackagesToScan(Product.class.getPackage().getName());
//        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
//        em.setJpaVendorAdapter(vendor);
//
//        Properties props = new Properties();
//        props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
//        props.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
//        props.setProperty("hibernate.show_sql", "false");
//        em.setJpaProperties(props);
//        return em;
//    }
//
//    @Primary
//    @Bean(name = "writeTransactionManager")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("readEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfb) {
//        JpaTransactionManager tm = new JpaTransactionManager();
//        tm.setEntityManagerFactory(emfb.getObject());
//        return tm;
//    }
//
//    @Bean(name = "writeTransactionTemplate")
//    public TransactionTemplate writeTransactionTemplate(
//            @Qualifier("writeTransactionManager") PlatformTransactionManager tm) {
//        return new TransactionTemplate(tm);
//    }
//}
