package br.com.eduardoenemark.rwt.app.server.repository;

import br.com.eduardoenemark.rwt.app.server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByProducer(String producer);

    List<Product> findByNameContaining(String name);

    long count();
}

