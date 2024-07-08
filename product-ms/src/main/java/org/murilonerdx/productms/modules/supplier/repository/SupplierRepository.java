package org.murilonerdx.productms.modules.supplier.repository;

import org.murilonerdx.productms.modules.supplier.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

	List<Supplier> findByNameIgnoreCaseContaining(String name);
}
