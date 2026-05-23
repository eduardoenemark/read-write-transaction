package br.com.eduardoenemark.rwt.core.operation;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperationTypeTest {

    @Test
    @Order(1)
    void testOperationTypeValues() {
        OperationType[] values = OperationType.values();
        assertEquals(2, values.length);
        assertEquals(OperationType.READ, values[0]);
        assertEquals(OperationType.WRITE, values[1]);
    }

    @Test
    @Order(2)
    void testOperationTypeValueOf() {
        assertEquals(OperationType.READ, OperationType.valueOf("READ"));
        assertEquals(OperationType.WRITE, OperationType.valueOf("WRITE"));
        assertThrows(IllegalArgumentException.class, () -> OperationType.valueOf("INVALID"));
    }

    @Test
    @Order(3)
    void testOperationTypeOrdinal() {
        assertEquals(0, OperationType.READ.ordinal());
        assertEquals(1, OperationType.WRITE.ordinal());
    }

    @Test
    @Order(4)
    void testOperationTypeToString() {
        assertEquals("READ", OperationType.READ.name());
        assertEquals("WRITE", OperationType.WRITE.name());
    }
}
