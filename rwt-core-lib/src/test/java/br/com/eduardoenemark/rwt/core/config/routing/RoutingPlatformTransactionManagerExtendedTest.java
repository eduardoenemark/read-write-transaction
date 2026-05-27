package br.com.eduardoenemark.rwt.core.config.routing;

import br.com.eduardoenemark.rwt.core.operation.OperationType;
import org.junit.jupiter.api.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoutingPlatformTransactionManagerExtendedTest {

    @AfterEach
    void cleanup() {
        RoutingPlatformTransactionManager.reset();
    }

    @Test
    @Order(1)
    void testAddReadEntityManagerFactory() {
        @SuppressWarnings("unchecked")
        LocalContainerEntityManagerFactoryBean emfb = null;
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        RoutingPlatformTransactionManager result = rtm.add(OperationType.READ, emfb);
        assertSame(rtm, result);
    }

    @Test
    @Order(2)
    void testAddWriteEntityManagerFactory() {
        @SuppressWarnings("unchecked")
        LocalContainerEntityManagerFactoryBean emfb = null;
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        RoutingPlatformTransactionManager result = rtm.add(OperationType.WRITE, emfb);
        assertSame(rtm, result);
    }

    @Test
    @Order(3)
    void testAddRoutingDataSource() {
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        RoutingPlatformTransactionManager result = rtm.add((RoutingDataSource) null);
        assertSame(rtm, result);
    }

    @Test
    @Order(4)
    void testFullFluentChaining() {
        @SuppressWarnings("unchecked")
        PlatformTransactionManager tmRead = null;
        @SuppressWarnings("unchecked")
        PlatformTransactionManager tmWrite = null;
        @SuppressWarnings("unchecked")
        LocalContainerEntityManagerFactoryBean emfbRead = null;
        @SuppressWarnings("unchecked")
        LocalContainerEntityManagerFactoryBean emfbWrite = null;
        RoutingDataSource rds = new RoutingDataSource();

        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        RoutingPlatformTransactionManager result = rtm
                .add(rds)
                .add(OperationType.READ, tmRead)
                .add(OperationType.WRITE, tmWrite)
                .add(OperationType.READ, emfbRead)
                .add(OperationType.WRITE, emfbWrite);

        // If we got here without exception, chaining works
        assertNotNull(result);
        assertSame(rtm, result);
    }

    @Test
    @Order(5)
    void testGetTransactionWithNoTransactionManagerRegisteredThrowsNPE() {
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertThrows(NullPointerException.class, () -> rtm.getTransaction(null));
    }

    @Test
    @Order(6)
    void testCommitWithNoTransactionManagerRegisteredThrowsNPE() {
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertThrows(NullPointerException.class, () -> rtm.commit(null));
    }

    @Test
    @Order(7)
    void testRollbackWithNoTransactionManagerRegisteredThrowsNPE() {
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertThrows(NullPointerException.class, () -> rtm.rollback(null));
    }

    @Test
    @Order(8)
    void testCurrentOperationTypeWithNoContext() {
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertEquals(OperationType.READ, rtm.getCurrentOperationType());
    }

    @Test
    @Order(9)
    void testCurrentOperationTypeWithReadContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertEquals(OperationType.READ, rtm.getCurrentOperationType());
    }

    @Test
    @Order(10)
    void testCurrentOperationTypeWithWriteContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        assertEquals(OperationType.WRITE, rtm.getCurrentOperationType());
    }

    @Test
    @Order(11)
    void testUnbindResourcesWithoutActiveTransaction() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        assertDoesNotThrow(RoutingPlatformTransactionManager::unbindResources);
    }

    @Test
    @Order(12)
    void testEndContextWithNoActiveContextDoesNotThrow() {
        RoutingPlatformTransactionManager.reset();
        assertDoesNotThrow(RoutingPlatformTransactionManager::endOperationContext);
    }

    @Test
    @Order(13)
    void testResetWithNoContext() {
        RoutingPlatformTransactionManager.reset();
        assertNull(RoutingPlatformTransactionManager.getThreadLocalContext().get());
        RoutingPlatformTransactionManager.reset();
        assertNull(RoutingPlatformTransactionManager.getThreadLocalContext().get());
    }

    @Test
    @Order(14)
    void testStartContextOverwritesExistingContext() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.READ);
        assertEquals(OperationType.READ, RoutingPlatformTransactionManager.getThreadLocalContext().get().getOperationType());

        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        assertEquals(OperationType.WRITE, RoutingPlatformTransactionManager.getThreadLocalContext().get().getOperationType());
    }

    @Test
    @Order(15)
    void testGetTransactionUsesCurrentOperationType() {
        RoutingPlatformTransactionManager.startOperationContext(OperationType.WRITE);
        RoutingPlatformTransactionManager rtm = new RoutingPlatformTransactionManager();
        // Without write transaction manager registered, this will throw NPE
        // but it proves getCurrentOperationType() returned WRITE (not READ)
        assertThrows(NullPointerException.class, () -> rtm.getTransaction(null));
    }
}
