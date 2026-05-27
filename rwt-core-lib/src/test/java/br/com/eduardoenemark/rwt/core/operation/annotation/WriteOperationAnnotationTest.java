package br.com.eduardoenemark.rwt.core.operation.annotation;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WriteOperationAnnotationTest {

    private class SampleService {
        @WriteOperation
        public void writeMethod() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Test
    @Order(1)
    void testAnnotationRetention() throws NoSuchMethodException {
        Method method = SampleService.class.getMethod("writeMethod");
        assertTrue(method.isAnnotationPresent(WriteOperation.class),
                "WriteOperation should be retained at runtime");
    }

    @Test
    @Order(2)
    void testAnnotationTargetIsMethod() {
        // Verify annotation targets METHOD
        java.lang.annotation.Target target = WriteOperation.class.getAnnotation(java.lang.annotation.Target.class);
        assertNotNull(target);
        assertEquals(1, target.value().length);
        assertEquals(java.lang.annotation.ElementType.METHOD, target.value()[0]);
    }

    @Test
    @Order(3)
    void testAnnotationIsMarker() throws NoSuchMethodException {
        Method method = SampleService.class.getMethod("writeMethod");
        WriteOperation ann = method.getAnnotation(WriteOperation.class);
        assertNotNull(ann);
    }

    @Test
    @Order(4)
    void testReadAndWriteAreDistinctAnnotations() throws NoSuchMethodException {
        class Service {
            @ReadOperation
            public void readMethod() {
                throw new UnsupportedOperationException();
            }

            @WriteOperation
            public void writeMethod() {
                throw new UnsupportedOperationException();
            }
        }

        Method readMethod = Service.class.getMethod("readMethod");
        Method writeMethod = Service.class.getMethod("writeMethod");

        assertTrue(readMethod.isAnnotationPresent(ReadOperation.class));
        assertTrue(writeMethod.isAnnotationPresent(WriteOperation.class));
        assertFalse(readMethod.isAnnotationPresent(WriteOperation.class));
        assertFalse(writeMethod.isAnnotationPresent(ReadOperation.class));
    }
}
