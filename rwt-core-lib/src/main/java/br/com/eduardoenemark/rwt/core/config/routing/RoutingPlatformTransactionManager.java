package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import lombok.Getter;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class RoutingPlatformTransactionManager implements PlatformTransactionManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(RoutingPlatformTransactionManager.class.getName());

    @Getter
    private static final ThreadLocal<OperationContext> threadLocalContext = new ThreadLocal<>();
    private static final Map<OperationType, PlatformTransactionManager> transactionManagers = new HashMap<>();
    private static final Map<OperationType, LocalContainerEntityManagerFactoryBean> localContainerEntityManagerFactories = new HashMap<>();
    private static RoutingDataSource routingDataSource;

    // Set context BEFORE transaction starts
    public static void bindResources(OperationType type) {
        LOGGER.debug("Binding resources for operation type {}", type);
        // Check if already bound with the same value (re-entrant @ReadOperation/@WriteOperation)
        Object existingType = TransactionSynchronizationManager.getResource(OperationType.class);
        if (existingType != null && existingType == type) {
            LOGGER.debug("OperationType {} already bound, skipping re-binding", type);
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            LOGGER.debug("No active transaction found, binding resources for operation type {}", type);
            startOperationContext(type);
            TransactionSynchronizationManager.bindResource(OperationType.class, type);
            TransactionSynchronizationManager.bindResource(DataSource.class, routingDataSource);
            TransactionSynchronizationManager.bindResource(RoutingDataSource.class, routingDataSource);
            TransactionSynchronizationManager.bindResource(RoutingPlatformTransactionManager.class, transactionManagers.get(type));
            TransactionSynchronizationManager.bindResource(LocalContainerEntityManagerFactoryBean.class, localContainerEntityManagerFactories.get(type));
        } else {
            LOGGER.debug("Active transaction found, skipping binding resources for operation type {}", type);
        }
    }

    // Reset AFTER transaction completes
    public static void unbindResources() {
        LOGGER.debug("Unbinding resources. Actual transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            endOperationContext();
            try {
                TransactionSynchronizationManager.unbindResource(OperationType.class);
            } catch (Exception e) { /* already unbound */ }
            try {
                TransactionSynchronizationManager.unbindResource(DataSource.class);
            } catch (Exception e) { /* already unbound */ }
            try {
                TransactionSynchronizationManager.unbindResource(RoutingDataSource.class);
            } catch (Exception e) { /* already unbound */ }
            try {
                TransactionSynchronizationManager.unbindResource(RoutingPlatformTransactionManager.class);
            } catch (Exception e) { /* already unbound */ }
            try {
                TransactionSynchronizationManager.unbindResource(LocalContainerEntityManagerFactoryBean.class);
            } catch (Exception e) { /* already unbound */ }
        } else {
            LOGGER.debug("Active transaction found, skipping resource unbind (will be cleaned up on transaction completion)");
        }
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        OperationType type = getCurrentOperationType();
        LOGGER.debug("Getting transaction for operation type {}", type);
        return transactionManagers.get(type).getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) {
        OperationType type = getCurrentOperationType();
        LOGGER.debug("Committing transaction for operation type {}", type);
        transactionManagers.get(type).commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) {
        OperationType type = getCurrentOperationType();
        LOGGER.debug("Rolling back transaction for operation type {}", type);
        transactionManagers.get(type).rollback(status);
    }

    public static void startOperationContext(OperationType type) {
        val context = new OperationContext();
        context.start(type);
        threadLocalContext.set(context);
    }

    public static void endOperationContext() {
        val context = threadLocalContext.get();
        if (context != null) {
            context.end();
        }
    }

    public static void reset() {
        val context = threadLocalContext.get();
        if (context != null) {
            threadLocalContext.remove();
        }
    }

    public static OperationType getCurrentOperationType() {
        val ctx = threadLocalContext.get();
        if (ctx != null && ctx.getOperationType() != null) {
            return ctx.getOperationType();
        }
        val dsCtx = RoutingDataSource.DatasourceContext.get();
        if (dsCtx != null) {
            return dsCtx;
        }
        return OperationType.READ;
    }

    public RoutingPlatformTransactionManager add(OperationType type, PlatformTransactionManager transactionManager) {
        transactionManagers.put(type, transactionManager);
        return this;
    }

    public RoutingPlatformTransactionManager add(OperationType type, LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        localContainerEntityManagerFactories.put(type, entityManagerFactoryBean);
        return this;
    }

    public RoutingPlatformTransactionManager add(RoutingDataSource routingDataSource) {
        RoutingPlatformTransactionManager.routingDataSource = routingDataSource;
        return this;
    }
}
