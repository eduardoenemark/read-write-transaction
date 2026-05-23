package br.com.eduardoenemark.rwt.app.server.operation;

import br.com.eduardoenemark.rwt.app.server.AppServerApplication;
import br.com.eduardoenemark.rwt.app.server.config.BaseTestConfiguration;
import br.com.eduardoenemark.rwt.app.server.entity.Product;
import br.com.eduardoenemark.rwt.app.server.service.ProductService;
import br.com.eduardoenemark.rwt.core.config.routing.RoutingTransaction;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


@Rollback(false)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = AppServerApplication.class,
        properties = {"logging.level.br.com.eduardoenemark.rwt.app.server=DEBUG"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ReadAndWriteOperationTest extends BaseTestConfiguration {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReadAndWriteOperationTest.class.getName());

    @Autowired
    ProductService productService;

    @Autowired
    @Qualifier("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate;

    @Autowired
    ApplicationContext context;

    @Test
    @Order(0)
    public void countEqualToZero() {
        executeReadOperation(() -> {
            val count = productService.count();
            assertEquals(0, count);
        });
    }

    @Test
    @Order(1)
    public void insertProduct() {
        executeWriteOperation(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            assertEquals(1, saved.getId());
        });
    }

    @Test
    @Order(2)
    public void insertAndCountProduct() {
        executeWriteOperation(() -> {
            val saved = productService.save(ProductService.fakeProduct());
            Assertions.assertTrue(saved.getId() > 1);
            val count = productService.count();
            assertEquals(2, count);
        });
    }

    @Test
    @Order(3)
    public void writeInReadOperation() {
        executeReadOperation(() -> {
            Assertions.assertThrows(
                    Exception.class,
                    () -> productService.save(ProductService.fakeProduct()));
        });
    }

    @Test
    @Order(4)
    public void commitInTransactionTemplateAndRollback() {
        val p1 = productService.findById(1);
        assertNotNull(p1, "Product 1 should exist from insertProduct test");
        p1.setName("Name01");
        productService.save(p1);

        writeTransactionTemplate.executeWithoutResult(status -> {
            p1.setName("Name02");
            productService.save(p1);
            status.flush();
        });
        assertEquals("Name02", p1.getName());
    }

    @SneakyThrows
    @Test
    @Order(5)
    public void operationsBetweenTransactionTemplate() {
        val productId = new java.util.concurrent.atomic.AtomicInteger();
        val t1 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);
        val t2 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);
        val t3 = context.getBean("writeTransactionTemplate", TransactionTemplate.class);

        t1.executeWithoutResult(status -> {
            LOGGER.info("Saving product");
            val saved = productService.save(ProductService.fakeProduct());
            productId.set(saved.getId());
            status.flush();
        });
        assertTrue(productId.get() > 0);

        AtomicBoolean exists = new AtomicBoolean(false);
        t2.execute(status -> {
            val product = productService.findById(productId.get());
            LOGGER.info("Finding product with id {}: {}", productId.get(), product);
            exists.set(product != null);
            status.flush();
            return product;
        });
        assertTrue(exists.get(), "Product should exist after t2 transaction");

        t3.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        t3.execute(status -> {
            LOGGER.info("Deleting product with id {}", productId.get());
            productService.deleteById(productId.get());
            status.flush();
            return null;
        });
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        LOGGER.info("Checking if product with id {} exists", productId.get());
        val productRef = new AtomicReference<Product>();
        executeReadOperation(() -> {
            productRef.set(productService.findById(productId.get()));
            LOGGER.info("Product with id {} exists: {}", productId.get(), productRef.get() != null);
        });
        assertNull(productRef.get(), "Product should be deleted after all transactions commit");
    }

    @SneakyThrows
    private void executeReadOperation(Runnable runnable) {
        val task = new Thread(() -> {
            try {
                RoutingTransaction.readBindResources();
                runnable.run();
            } finally {
                RoutingTransaction.readUnbindResources();
            }
        });
        task.start();
        task.join();
    }

    @SneakyThrows
    private void executeWriteOperation(Runnable runnable) {
        val task = new Thread(() -> {
            try {
                RoutingTransaction.writeBindResources();
                runnable.run();
            } finally {
                RoutingTransaction.readUnbindResources();
            }
        });
        task.start();
        task.join();
    }
}
