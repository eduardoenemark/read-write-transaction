package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.UserTransaction;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import static br.com.eduardoenemark.rwt.core.config.routing.AppCoreConstants.*;

@Configuration
public class TransactionManagerBeansConfiguration {

    public static final String READ_TRANSACTION_MANAGER = "readTransactionManager";
    public static final String WRITE_TRANSACTION_MANAGER = "writeTransactionManager";

    @Primary
    @Bean(name = {ROUTING_DATASOURCE, "dataSource"})
    public AbstractRoutingDataSource routingDataSource(@Qualifier(READ_DATASOURCE) DataSource readDataSource,
                                                       @Qualifier(WRITE_DATASOURCE) DataSource writeDataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(
                Collections.unmodifiableMap(
                        new HashMap<OperationType, DataSource>() {{
                            put(OperationType.READ, readDataSource);
                            put(OperationType.WRITE, writeDataSource);
                        }}));
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        return routingDataSource;
    }

    @Primary
    @Bean("transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("routingDataSource") RoutingDataSource routingDataSource,
            @Qualifier("readEntityManagerFactory") LocalContainerEntityManagerFactoryBean readEntityManagerFactory,
            @Qualifier("writeEntityManagerFactory") LocalContainerEntityManagerFactoryBean writeEntityManagerFactory,
            @Qualifier("readTransactionManager") PlatformTransactionManager readTransactionManager,
            @Qualifier("writeTransactionManager") PlatformTransactionManager writeTransactionManager) {
        return new RoutingPlatformTransactionManager()
                .add(routingDataSource)
                .add(OperationType.READ, readEntityManagerFactory)
                .add(OperationType.WRITE, writeEntityManagerFactory)
                .add(OperationType.READ, readTransactionManager)
                .add(OperationType.WRITE, writeTransactionManager);
    }

    @Bean(name = "writeTransactionTemplate")
    public TransactionTemplate writeTransactionTemplate(@Qualifier("writeTransactionManager") PlatformTransactionManager writeTransactionManager) {
        return new TransactionTemplate(writeTransactionManager) {
            @Override
            public void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
                if (RoutingDataSource.DatasourceContext.get() == null || !OperationType.WRITE.equals(RoutingDataSource.DatasourceContext.get())) {
                    RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
                    try {
                        super.executeWithoutResult(action);
                    } finally {
                        RoutingDataSource.DatasourceContext.reset();
                    }
                } else {
                    super.executeWithoutResult(action);
                }
            }

            @Override
            public <T> T execute(TransactionCallback<T> action) throws TransactionException {
                if (RoutingDataSource.DatasourceContext.get() == null || !OperationType.WRITE.equals(RoutingDataSource.DatasourceContext.get())) {
                    RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
                    try {
                        return super.execute(action);
                    } finally {
                        RoutingDataSource.DatasourceContext.reset();
                    }
                } else {
                    return super.execute(action);
                }
            }
        };
    }

    @Bean(name = READ_TRANSACTION_MANAGER)
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("routingDataSource") DataSource dataSource,
            @Qualifier("readEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfb) {
        val tm = new JpaTransactionManager();
        tm.setDataSource(dataSource);
        tm.setEntityManagerFactory(emfb.getObject());
        return tm;
    }

    @Bean(name = WRITE_TRANSACTION_MANAGER)
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("userTransaction") UserTransaction userTransaction,
            @Qualifier("userTransactionManager") UserTransactionManager userTransactionManager) {
        val tm = new JtaTransactionManager();
        tm.setUserTransaction(userTransaction);
        tm.setTransactionManager(userTransactionManager);
        tm.setGlobalRollbackOnParticipationFailure(true);
        tm.setRollbackOnCommitFailure(true);
        tm.setFailEarlyOnGlobalRollbackOnly(true);
        tm.setTransactionSynchronization(JtaTransactionManager.SYNCHRONIZATION_ALWAYS);
        return tm;
    }

    @Bean(name = "userTransactionManager", initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager() {
        UserTransactionManager utm = new UserTransactionManager();
        utm.setForceShutdown(false);
        utm.setStartupTransactionService(true);
        return utm;
    }

    @Bean(name = "userTransaction")
    public UserTransactionImp userTransaction() {
        return new UserTransactionImp();
    }
}
