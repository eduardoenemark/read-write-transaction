package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static br.com.eduardoenemark.rwt.core.config.routing.AppCoreConstants.LOGGER;

public class RoutingTransaction {

    public static void readBindResources() {
        LOGGER.debug("Read operation binding resources");
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        RoutingPlatformTransactionManager.bindResources(OperationType.READ);
    }

    public static void readUnbindResources() {
        LOGGER.debug("Read operation unbinding resources");
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
    }

    public static void writeBindResources() {
        LOGGER.debug("Write operation binding resources");
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingPlatformTransactionManager.bindResources(OperationType.WRITE);
    }

    public static void writeUnbindResources() {
        LOGGER.debug("Write operation unbinding resources");
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.unbindResources();
    }

    public static boolean isActiveTransaction() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public static void clear() {
        LOGGER.debug("Clearing transaction");
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.clear();
    }

    public static void init() {
        LOGGER.debug("Initializing synchronization");
        TransactionSynchronizationManager.initSynchronization();
    }

    public static void clearAndInit() {
        clear();
        init();
    }
}
