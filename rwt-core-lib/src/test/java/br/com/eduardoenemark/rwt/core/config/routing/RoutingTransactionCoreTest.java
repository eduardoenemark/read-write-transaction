package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingTransactionCoreTest {

    @AfterEach
    void cleanup() {
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testDatasourceContextSetDirectlyToRead() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(2)
    void testDatasourceContextSetDirectlyToWrite() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(3)
    void testDatasourceContextResetReturnsToDefaultRead() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingDataSource.DatasourceContext.reset();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
        assertSame(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(4)
    void testDatasourceContextMultipleOverwrites() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(5)
    void testUnbindResourcesViaRoutingTransactionResetsContext() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingTransaction.readUnbindResources();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());

        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingTransaction.writeUnbindResources();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(6)
    void testEndOperationContextSetsEndTime() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        OperationContext ctxBefore = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertEquals(0L, ctxBefore.getEndOperationMillis());

        RoutingPlatformTransactionManager.endOperationContext();
        OperationContext ctxAfter = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertTrue(ctxAfter.getEndOperationMillis() > 0);
    }

    @Test
    @Order(7)
    void testEndContextWithNoActiveContextDoesNotThrow() {
        RoutingPlatformTransactionManager.reset();
        assertDoesNotThrow(RoutingPlatformTransactionManager::endOperationContext);
    }

    @Test
    @Order(8)
    void testIsActiveTransactionIsFalseOutsideSpringContext() {
        assertFalse(RoutingTransaction.isActiveTransaction());
    }

    @Test
    @Order(9)
    void testDatasourceContextIsThreadIsolated() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);

        Thread background = new Thread(() -> {
            assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
            RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
            assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
        });

        background.start();
        try {
            background.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get(),
                "Main thread context should remain READ");
    }

    @Test
    @Order(10)
    void testOperationContextDurationAfterManualStartAndEnd() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        OperationContext ctx = RoutingPlatformTransactionManager.getThreadLocalContext().get();

        assertTrue(ctx.getStartOperationMillis() > 0);
        long beforeEnd = System.currentTimeMillis();
        ctx.end();
        assertTrue(ctx.getEndOperationMillis() >= beforeEnd);
    }
}
