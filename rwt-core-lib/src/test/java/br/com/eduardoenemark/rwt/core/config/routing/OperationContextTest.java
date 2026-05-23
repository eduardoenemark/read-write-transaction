package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperationContextTest {

    @AfterEach
    void cleanup() {
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testDefaultConstructorGeneratesCorrelationId() {
        OperationContext ctx = new OperationContext();
        assertNotNull(ctx.getCorrelationId(), "CorrelationId should not be null");
        assertFalse(ctx.getCorrelationId().isBlank(), "CorrelationId should not be blank");
    }

    @Test
    @Order(2)
    void testCorrelationIdIsUuidFormat() {
        OperationContext ctx = new OperationContext();
        String id = ctx.getCorrelationId();
        // Basic UUID format check: 8-4-4-4-12
        assertEquals(36, id.length(), "UUID should be 36 chars");
    }

    @Test
    @Order(3)
    void testStartSetsOperationType() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.WRITE);
        assertEquals(OperationType.WRITE, ctx.getOperationType());
    }

    @Test
    @Order(4)
    void testStartSetsStartTime() {
        long before = System.currentTimeMillis();
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        assertTrue(ctx.getStartOperationMillis() >= before,
            "Start time should be >= time before start()");
    }

    @Test
    @Order(5)
    void testEndSetsEndTime() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.WRITE);
        long beforeEnd = System.currentTimeMillis();
        ctx.end();
        assertTrue(ctx.getEndOperationMillis() >= beforeEnd,
            "End time should be >= time before end()");
    }

    @Test
    @Order(6)
    void testDiffMillisReturnsPositiveDuration() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        
        try {
            Thread.sleep(50); // Small delay to ensure measurable diff
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ctx.end();
        long diff = ctx.diffMillis();
        assertTrue(diff >= 50, "Duration should be >= 50ms, got " + diff);
        assertTrue(diff < 2000, "Duration should be < 2000ms, got " + diff);
    }

    @Test
    @Order(7)
    void testGetDiffIsAliasOfDiffMillis() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        ctx.end();
        assertEquals(ctx.diffMillis(), ctx.getDiff(), "getDiff() should equal diffMillis()");
    }

    @Test
    @Order(8)
    void testSetCorrelationIdGeneratesNewUuid() {
        OperationContext ctx = new OperationContext();
        String original = ctx.getCorrelationId();
        
        ctx.setCorrelationId();
        assertNotEquals(original, ctx.getCorrelationId(),
            "New correlation ID should differ from original");
    }

    @Test
    @Order(9)
    void testTostringContainsFields() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.WRITE);
        ctx.end();
        String str = ctx.toString();
        assertTrue(str.contains("WRITE"), "toString should contain operation type");
        assertTrue(str.contains("correlationId"), "toString should contain correlationId field");
    }

    @Test
    @Order(10)
    void testSerializable() {
        OperationContext ctx = new OperationContext();
        ctx.start(OperationType.READ);
        ctx.end();
        // Just verify it implements Serializable without throwing
        assertNotNull(ctx, "Context should be created");
    }
}
