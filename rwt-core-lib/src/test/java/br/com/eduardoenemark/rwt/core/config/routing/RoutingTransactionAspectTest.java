package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingTransactionAspectTest {

    private RoutingTransactionAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RoutingTransactionAspect();
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }

    @AfterEach
    void tearDown() {
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testAspectInstanceCreated() {
        assertNotNull(aspect);
    }

    @Test
    @Order(2)
    void testReadUnbindResetsContextToDefault() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingTransaction.readUnbindResources();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(3)
    void testWriteUnbindResetsContextToDefault() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        RoutingTransaction.writeUnbindResources();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(4)
    void testUnbindResourcesOnCleanContextDoesNotThrow() {
        RoutingDataSource.DatasourceContext.reset();
        RoutingPlatformTransactionManager.reset();
        assertDoesNotThrow(RoutingTransaction::readUnbindResources);
    }

    @Test
    @Order(5)
    void testReadThenUnbindReadCleansState() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        RoutingTransaction.readUnbindResources();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(6)
    void testUnbindIsIdempotentOnError() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        RoutingPlatformTransactionManager.unbindResources();
        // Second unbind without any active tx should handle gracefully
        assertDoesNotThrow(RoutingPlatformTransactionManager::unbindResources);
    }
}
