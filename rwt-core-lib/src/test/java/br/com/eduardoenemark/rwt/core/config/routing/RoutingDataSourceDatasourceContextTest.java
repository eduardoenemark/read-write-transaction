package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingDataSourceDatasourceContextTest {

    @AfterEach
    void cleanup() {
        RoutingDataSource.DatasourceContext.reset();
    }

    @Test
    @Order(1)
    void testDefaultGetReturnsReadWhenNoContextSet() {
        // Reset first to ensure clean state
        RoutingDataSource.DatasourceContext.reset();
        // get() defaults to READ when ThreadLocal is null
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(2)
    void testSetAndGetRead() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(3)
    void testSetAndGetWrite() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(4)
    void testResetThenGetReturnsDefaultRead() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
        
        RoutingDataSource.DatasourceContext.reset();
        // After reset, get() should return default READ
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(5)
    void testMultipleSetOverwrites() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
        
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingDataSource.DatasourceContext.get());
        
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(6)
    void testDefaultBehavior() {
        // Without any set(), get() should return READ as default
        RoutingDataSource.DatasourceContext.reset();
        assertEquals(OperationType.READ, RoutingDataSource.DatasourceContext.get());
    }
}
