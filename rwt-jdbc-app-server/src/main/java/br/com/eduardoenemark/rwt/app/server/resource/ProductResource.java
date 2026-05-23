package br.com.eduardoenemark.rwt.app.server.resource;

import br.com.eduardoenemark.rwt.core.operation.annotation.ReadOperation;
import br.com.eduardoenemark.rwt.core.operation.annotation.WriteOperation;
import br.com.eduardoenemark.rwt.app.server.entity.Product;
import br.com.eduardoenemark.rwt.app.server.service.ProductService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static br.com.eduardoenemark.rwt.app.server.service.ProductService.fakeProduct;

@RestController
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductResource {

    ProductService service;

    @Autowired
    public ProductResource(ProductService service) {
        this.service = service;
    }

    @WriteOperation
    @PostMapping("/product")
    public ResponseEntity<Product> save(@RequestBody Product product) {
        product.setId(null);
        Product savedProduct = service.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @WriteOperation
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @WriteOperation
    @DeleteMapping("/products")
    public ResponseEntity<Void> deleteAll() {
        service.deleteAll();
        return ResponseEntity.ok().build();
    }

    @ReadOperation
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        Product product = service.findById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @ReadOperation
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @ReadOperation
    @GetMapping("/products/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(service.count());
    }

    @GetMapping("/product/generate")
    public ResponseEntity<Product> generate() {
        return ResponseEntity.ok(fakeProduct());
    }

    @WriteOperation
    @PostMapping("/product/generate-and-save")
    public ResponseEntity<Product> generateAndSave() {
        val saved = this.service.save(fakeProduct());
        return ResponseEntity.ok(saved);
    }


}
