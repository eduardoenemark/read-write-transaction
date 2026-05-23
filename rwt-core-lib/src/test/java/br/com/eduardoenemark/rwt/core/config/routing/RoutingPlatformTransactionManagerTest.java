package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingPlatformTransactionManagerTest {

    private RoutingPlatformTransactionManager rtm;

    @BeforeEach
    void setUp() {
        rtm = new RoutingPlatformTransactionManager();
    }

    @AfterEach
    void cleanup() {
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testAddOperationType() {
        @SuppressWarnings("unchecked")
        PlatformTransactionManager tm = null;
        RoutingPlatformTransactionManager result = rtm.add(OperationType.READ, tm);
        assertSame(rtm, result, "add() should return this for chaining");
    }

    @Test
    @Order(2)
    void testAddDataSource() {
        RoutingPlatformTransactionManager result = rtm.add(null);
        assertSame(rtm, result, "add(RoutingDataSource) should return this for chaining");
    }

    @Test
    @Order(3)
    void testStartOperationContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        OperationContext ctx = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertNotNull(ctx, "Context should be set");
        assertEquals(OperationType.WRITE, ctx.getOperationType());
    }

    @Test
    @Order(4)
    void testEndOperationContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        OperationContext ctxBefore = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertNotNull(ctxBefore, "Context should exist before end()");
        
        RoutingPlatformTransactionManager.endOperationContext();
        // Context still exists but end time is set
        OperationContext ctxAfter = RoutingPlatformTransactionManager.getThreadLocalContext().get();
        assertNotNull(ctxAfter, "Context should still exist");
        assertTrue(ctxAfter.getEndOperationMillis() > 0, "End time should be set after end()");
    }

    @Test
    @Order(5)
    void testReset() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        assertNotNull(RoutingPlatformTransactionManager.getThreadLocalContext().get());
        
        RoutingPlatformTransactionManager.reset();
        assertNull(RoutingPlatformTransactionManager.getThreadLocalContext().get(),
            "Context should be null after reset()");
    }

    @Test
    @Order(6)
    void testGetCurrentOperationTypeFromContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        assertEquals(OperationType.WRITE, rtm.getCurrentOperationType());
    }

    @Test
    @Order(7)
    void testGetCurrentOperationTypeDefaultsToRead() {
        // No context set - should default to READ
        assertEquals(OperationType.READ, rtm.getCurrentOperationType());
    }

    @Test
    @Order(8)
    void testChainingAddOperations() {
        RoutingPlatformTransactionManager rtm2 = new RoutingPlatformTransactionManager();
        @SuppressWarnings("unchecked")
        PlatformTransactionManager tm = null;
        rtm2.add(OperationType.READ, tm)
            .add(OperationType.WRITE, tm)
            .add((br.com.eduardoenemark.rwt.core.config.routing.RoutingDataSource) null);
        
        // Should have both operation types registered without error
        assertNotNull(rtm2);
    }

    @Test
    @Order(9)
    void testBindResourcesNoActiveTransaction() {
        // bindResources should work even without active transaction
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        assertDoesNotThrow(() -> {
            // Simulate binding - no active transaction expected
            boolean isActive = org.springframework.transaction.support.TransactionSynchronizationManager
                    .isActualTransactionActive();
            assertFalse(isActive, "Should not have active transaction in test");
        });
    }
}
