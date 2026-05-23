package br.com.eduardoenemark.rwt.app.server.resource;

import br.com.eduardoenemark.rwt.app.server.AppServerApplication;
import br.com.eduardoenemark.rwt.app.server.config.BaseTestConfiguration;
import br.com.eduardoenemark.rwt.app.server.entity.Product;
import br.com.eduardoenemark.rwt.app.server.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(
        classes = {AppServerApplication.class},
        properties = {"spring.profiles.active=test", "logging.level.br.com.eduardoenemark.rwt.app.server=INFO"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class ProductResourceTest extends BaseTestConfiguration {

    @Autowired
    ProductService service;

    @Autowired
    @Qualifier("writeTransactionTemplate")
    TransactionTemplate writeTransactionTemplate;

    @BeforeEach
    void setUp() {
        service.deleteAll();
    }

    @Test
    @Order(0)
    void testSaveProduct() {
        Product product = new Product()
                .setName("Test Product")
                .setPrice(BigDecimal.valueOf(99.99))
                .setAmount(10)
                .setCountry("BR")
                .setUniversalProductCode("123456789012")
                .setEntryDate(LocalDate.now())
                .setProducer("Test Producer");

        Product saved = service.save(product);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Product", saved.getName());
    }

    @Test
    @Order(1)
    void testGetProductById() {
        Product saved = saveTestProduct();
        Product found = service.findById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Test Product", found.getName());
    }

    @Test
    @Order(2)
    void testGetAllProducts() {
        saveTestProduct();
        saveTestProduct();

        List<Product> all = service.findAll();
        assertNotNull(all);
        assertTrue(all.size() >= 2);
    }

    @Test
    @Order(3)
    void testCountProducts() {
        saveTestProduct();
        long count = service.count();
        assertTrue(count >= 1);
    }

    @Test
    @Order(4)
    void testDeleteProduct() {
        Product saved = saveTestProduct();
        service.deleteById(saved.getId());

        Product found = service.findById(saved.getId());
        assertNull(found);
    }

    @Test
    @Order(5)
    void testDeleteAllProducts() {
        saveTestProduct();
        service.deleteAll();

        long count = service.count();
        assertEquals(0, count);
    }

    @Test
    @Order(6)
    void testGenerateProduct() {
        Product generated = service.generate();
        assertNotNull(generated);
        assertNotNull(generated.getName());
        assertNotNull(generated.getPrice());
    }

    private Product saveTestProduct() {
        Product product = new Product()
                .setName("Test Product")
                .setPrice(BigDecimal.valueOf(99.99))
                .setAmount(10)
                .setCountry("BR")
                .setUniversalProductCode("123456789012")
                .setEntryDate(LocalDate.now())
                .setProducer("Test Producer");

        writeTransactionTemplate.executeWithoutResult(status -> {
            service.save(product);
            status.flush();
        });
        return product;
    }
}
