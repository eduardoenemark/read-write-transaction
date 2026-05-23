package br.com.eduardoenemark.rwt.app.server.service;

import br.com.eduardoenemark.rwt.app.server.entity.Product;
import br.com.eduardoenemark.rwt.core.operation.annotation.ReadOperation;
import br.com.eduardoenemark.rwt.core.operation.annotation.WriteOperation;
import br.com.eduardoenemark.rwt.app.server.repository.ProductRepository;
import com.github.javafaker.Faker;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductService {

    ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @ReadOperation
    public List<Product> all() {
        return repository.findAll();
    }

    @ReadOperation
    public List<Product> findAll() {
        return repository.findAll();
    }

    @WriteOperation
    public Product save(Product product) {
        return repository.save(product);
    }

    @ReadOperation
    public void deleteAll() {
        repository.deleteAll();
    }

    @WriteOperation
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    @ReadOperation
    public Product findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @ReadOperation
    public long count() {
        return repository.count();
    }

    @ReadOperation
    public Product generate() {
        val faker = new Faker();
        var product = new Product();
        product.setName(faker.commerce().productName());
        product.setPrice(BigDecimal.valueOf(faker.number().randomDouble(1, 2, 1000)));
        product.setAmount(faker.number().numberBetween(1, 100));
        product.setCountry(faker.address().country());
        product.setUniversalProductCode(faker.code().ean8());
        product.setEntryDate(LocalDate.now());
        product.setProducer(faker.company().name());
        return save(product);
    }

    public static Product fakeProduct() {
        val faker = new Faker();
        val product = new Product();
        product.setName(faker.commerce().productName());
        product.setPrice(BigDecimal.valueOf(faker.number().randomDouble(1, 2, 1000)));
        product.setAmount(faker.number().numberBetween(1, 100));
        product.setCountry(faker.address().country());
        product.setUniversalProductCode(faker.code().ean8());
        product.setEntryDate(LocalDate.now());
        product.setProducer(faker.company().name());
        return product;
    }
}
