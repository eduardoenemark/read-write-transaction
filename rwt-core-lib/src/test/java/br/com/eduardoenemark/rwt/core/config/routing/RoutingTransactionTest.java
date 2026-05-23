package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingTransactionTest {

    @AfterEach
    void cleanup() {
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testIsActiveTransactionReturnsFalseWithoutSpringContext() {
        assertFalse(RoutingTransaction.isActiveTransaction(),
            "Should be false without active Spring transaction");
    }

    @Test
    @Order(2)
    void testDatasourceContextIsSetBeforeBinding() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
        
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
        
        RoutingDataSource.DatasourceContext.reset();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(3)
    void testPlatformTransactionManagerStartAndReset() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        OperationContext ctx = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertNotNull(ctx);
        assertEquals(OperationType.WRITE, ctx.getOperationType());
        
        RoutingPlatformTransactionManager.reset();
        assertNull(RoutingPlatformTransactionManager.getThreadLocalContext().get());
    }
}
