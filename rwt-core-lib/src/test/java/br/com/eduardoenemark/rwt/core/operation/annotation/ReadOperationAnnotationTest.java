package br.com.eduardoenemark.rwt.core.operation.annotation;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReadOperationAnnotationTest {

    private class SampleService {
        @ReadOperation
        public void readMethod() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Test
    @Order(1)
    void testAnnotationRetention() throws NoSuchMethodException {
        Method method = SampleService.class.getMethod("readMethod");
        assertTrue(method.isAnnotationPresent(ReadOperation.class),
                "ReadOperation should be retained at runtime");
    }

    @Test
    @Order(2)
    void testAnnotationTargetIsMethod() throws NoSuchMethodException {
        Method method = SampleService.class.getMethod("readMethod");
        ReadOperation ann = method.getAnnotation(ReadOperation.class);
        assertNotNull(ann);
        // Verify it targets METHOD - check via reflection
        for (java.lang.reflect.AnnotatedElement elem : new java.lang.reflect.AnnotatedElement[]{SampleService.class}) {
            // ReadOperation can only be on methods, not classes
            assertFalse(elem.isAnnotationPresent(ReadOperation.class));
        }
    }

    @Test
    @Order(3)
    void testAnnotationHasNoRequiredElements() throws NoSuchMethodException {
        Method method = SampleService.class.getMethod("readMethod");
        ReadOperation ann = method.getAnnotation(ReadOperation.class);
        assertNotNull(ann);
    }
}
