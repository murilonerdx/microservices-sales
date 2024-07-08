package org.murilonerdx.productms.modules.product.repository;

import org.murilonerdx.productms.modules.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	List<Product> findByNameIgnoreCaseContaining(String name);

	List<Product> findByCategoryId(Integer id);

	List<Product> findBySupplierId(Integer id);

	Boolean existsByCategoryId(Integer id);

	Boolean existsBySupplierId(Integer id);
}
