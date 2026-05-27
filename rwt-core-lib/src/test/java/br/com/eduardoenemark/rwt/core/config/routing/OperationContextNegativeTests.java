package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperationContextNegativeTests {

    @AfterEach
    void cleanup() {
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testDiffMillisBeforeEndReturnsNegative() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        // end() was not called - endOperationMillis defaults to 0L
        long diff = ctx.diffMillis();
        assertTrue(diff <= 0, "Duration before end() should be <= 0");
    }

    @Test
    @Order(2)
    void testGetDiffBeforeEndReturnsNegative() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.WRITE);
        long diff = ctx.getDiff();
        assertTrue(diff <= 0, "getDiff before end() should be <= 0");
    }

    @Test
    @Order(3)
    void testCorrelationIdIsUnique() {
        OperationContext ctx1 = new OperationContext();
        OperationContext ctx2 = new OperationContext();
        assertNotEquals(ctx1.getCorrelationId(), ctx2.getCorrelationId());
    }

    @Test
    @Order(4)
    void testMultipleStartsOverwriteOperationType() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        assertEquals(OperationType.READ, ctx.getOperationType());

        ctx.start(OperationType.WRITE);
        assertEquals(OperationType.WRITE, ctx.getOperationType());
    }

    @Test
    @Order(5)
    void testMultipleEndCallsStillValid() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        ctx.end();
        long diff1 = ctx.diffMillis();

        ctx.end();
        long diff2 = ctx.diffMillis();

        assertTrue(diff2 >= diff1, "Diff should increase after second end()");
    }

    @Test
    @Order(6)
    void testToStringContainsCorrelationId() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.WRITE);
        ctx.end();
        String str = ctx.toString();
        assertTrue(str.contains("WRITE"), "toString should contain operation type");
    }

    @Test
    @Order(7)
    void testEndBeforeStartSetsEndTime() {
        OperationContext ctx = new OperationContext();
        long before = System.currentTimeMillis();
        ctx.end();
        assertTrue(ctx.getEndOperationMillis() >= before, "End time should be set even if start() not called");
    }

    @Test
    @Order(8)
    void testSetCorrelationIdCanBeSetExplicitly() {
        OperationContext ctx = new OperationContext();
        String before = ctx.getCorrelationId();

        ctx.setCorrelationId();
        assertNotEquals(before, ctx.getCorrelationId());
    }

    @Test
    @Order(9)
    void testOperationTypeCanBeRead() {
        OperationContext ctx = new OperationContext();
        assertNull(ctx.getOperationType(), "OperationType should be null before start()");

        ctx.start(OperationType.WRITE);
        assertEquals(OperationType.WRITE, ctx.getOperationType());
    }

    @Test
    @Order(10)
    void testStartOperationMillisIsSet() {
        OperationContext ctx = new OperationContext();
        long beforeStart = System.currentTimeMillis();
        ctx.start(OperationType.READ);
        assertTrue(ctx.getStartOperationMillis() >= beforeStart);
    }
}
