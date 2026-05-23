package br.com.eduardoenemark.rwt.app.server.config;

import br.com.eduardoenemark.rwt.app.server.entity.Product;
import com.atomikos.jdbc.AtomikosNonXADataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static br.com.eduardoenemark.rwt.core.config.routing.AppCoreConstants.*;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.eduardoenemark.rwt.app.server.repository")
public class DataSourceBeansConfiguration {

    @Bean(name = READ_DATASOURCE)
    public DataSource readDataSource(PropsConfig.DataSourcePropsConfig dataSourcePropsConfig,
                                     @Qualifier("poolReadPropsConfig") PropsConfig.DataSourcePropsConfig.PoolPropsConfig poolReadPropsConfig) {
        val dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourcePropsConfig.getUrl());
        dataSource.setDriverClassName(dataSourcePropsConfig.getDriverClassName());
        dataSource.setUsername(dataSourcePropsConfig.getUsername());
        dataSource.setPassword(dataSourcePropsConfig.getPassword());
        dataSource.setMinimumIdle(poolReadPropsConfig.getMinimumIdle());
        dataSource.setMaximumPoolSize(poolReadPropsConfig.getMaximumSize());
        dataSource.setMinimumIdle(poolReadPropsConfig.getMinimumSize());
        dataSource.setConnectionTimeout(poolReadPropsConfig.getBorrowConnectionTimeoutSecs() * 1000L);
        dataSource.setIdleTimeout(poolReadPropsConfig.getMaxIdleTimeSecs() * 1000L);
        dataSource.setInitializationFailTimeout(poolReadPropsConfig.getInitializationFailTimeoutSecs() * 1000L);
        dataSource.setPoolName(poolReadPropsConfig.getName());
        dataSource.setReadOnly(true);
        return dataSource;
    }

    @Bean(name = WRITE_DATASOURCE)
    public DataSource writeDataSource(PropsConfig.DataSourcePropsConfig dataSourcePropsConfig,
                                      @Qualifier("poolWritePropsConfig") PropsConfig.DataSourcePropsConfig.PoolPropsConfig poolWritePropsConfig) throws SQLException {
        val dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUrl(dataSourcePropsConfig.getUrl());
        dataSource.setUser(dataSourcePropsConfig.getUsername());
        dataSource.setPassword(dataSourcePropsConfig.getPassword());
        dataSource.setDriverClassName(dataSourcePropsConfig.getDriverClassName());
        dataSource.setUniqueResourceName(poolWritePropsConfig.getName());
        dataSource.setLoginTimeout(poolWritePropsConfig.getInitializationFailTimeoutSecs());
        dataSource.setMaxPoolSize(poolWritePropsConfig.getMaximumSize());
        dataSource.setMinPoolSize(poolWritePropsConfig.getMinimumSize());
        dataSource.setBorrowConnectionTimeout(poolWritePropsConfig.getBorrowConnectionTimeoutSecs() * 1000);
        dataSource.setMaxIdleTime(poolWritePropsConfig.getMaxIdleTimeSecs() * 1000);
        dataSource.setTestQuery(poolWritePropsConfig.getConnectionTestQuery());
        dataSource.setIgnoreJtaTransactions(true);
        dataSource.setReadOnly(false);
        dataSource.setLocalTransactionMode(true);
        return dataSource;
    }

    @Primary
    @Bean(name = READ_HIBERNATE)
    @ConfigurationProperties("datasource.pool.read")
    public Properties readHibernate() {
        return new Properties();
    }

    @Bean(name = WRITE_HIBERNATE)
    @ConfigurationProperties("datasource.pool.write")
    public Properties writeHibernate() {
        return new Properties();
    }

    @Primary
    @Bean(name = {READ_ENTITY_MANAGER_FACTORY, "entityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(@Qualifier(ROUTING_DATASOURCE) DataSource dataSource,
                                                                           @Qualifier("readHibernate") Properties hibernateProperties) {
        return this.entityManagerFactory(dataSource, hibernateProperties);
    }

    @Bean(name = WRITE_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(@Qualifier("routingDataSource") DataSource dataSource,
                                                                            @Qualifier("writeHibernate") Properties hibernateProperties) {
        return this.entityManagerFactory(dataSource, hibernateProperties);
    }

    private LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                        Properties hibernateProperties) {
        val em = new LocalContainerEntityManagerFactoryBean();
        val vendor = new HibernateJpaVendorAdapter();
        em.setDataSource(dataSource);
        em.setPackagesToScan(Product.class.getPackage().getName());
        em.setJpaVendorAdapter(vendor);
        em.setJpaProperties(hibernateProperties);
        return em;
    }
}
