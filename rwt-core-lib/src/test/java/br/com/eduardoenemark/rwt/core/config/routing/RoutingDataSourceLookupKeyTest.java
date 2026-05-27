package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingDataSourceLookupKeyTest {

    private RoutingDataSource routingDataSource;

    @BeforeEach
    void setUp() {
        routingDataSource = new RoutingDataSource();
        RoutingDataSource.DatasourceContext.reset();
    }

    @AfterEach
    void tearDown() {
        RoutingDataSource.DatasourceContext.reset();
    }

    @Test
    @Order(1)
    void testDetermineLookupKeyDefaultsToRead() {
        RoutingDataSource.DatasourceContext.reset();
        Object lookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(OperationType.READ, lookupKey);
    }

    @Test
    @Order(2)
    void testDetermineLookupKeyReturnsReadWhenSet() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        Object lookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(OperationType.READ, lookupKey);
    }

    @Test
    @Order(3)
    void testDetermineLookupKeyReturnsWriteWhenSet() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        Object lookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(OperationType.WRITE, lookupKey);
    }

    @Test
    @Order(4)
    void testDetermineLookupKeyAfterResetDefaultsToRead() {
        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        RoutingDataSource.DatasourceContext.reset();
        Object lookupKey = routingDataSource.determineCurrentLookupKey();
        assertEquals(OperationType.READ, lookupKey);
    }

    @Test
    @Order(5)
    void testDetermineLookupKeyConsistencyWithDatasourceContext() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        assertEquals(routingDataSource.determineCurrentLookupKey(), RoutingDataSource.DatasourceContext.get());

        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        assertEquals(routingDataSource.determineCurrentLookupKey(), RoutingDataSource.DatasourceContext.get());
    }

    @Test
    @Order(6)
    void testMultipleLookupsWithSameContext() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        Object key1 = routingDataSource.determineCurrentLookupKey();
        Object key2 = routingDataSource.determineCurrentLookupKey();
        assertSame(key1, key2);
    }

    @Test
    @Order(7)
    void testLookupKeyChangesAfterOverwrite() {
        RoutingDataSource.DatasourceContext.set(OperationType.READ);
        Object readKey = routingDataSource.determineCurrentLookupKey();

        RoutingDataSource.DatasourceContext.set(OperationType.WRITE);
        Object writeKey = routingDataSource.determineCurrentLookupKey();

        assertNotSame(readKey, writeKey);
    }
}
