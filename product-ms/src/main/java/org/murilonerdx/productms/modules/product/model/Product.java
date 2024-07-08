package org.murilonerdx.productms.modules.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.murilonerdx.productms.modules.category.model.Category;
import org.murilonerdx.productms.modules.product.dto.ProductRequest;
import org.murilonerdx.productms.modules.supplier.model.Supplier;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name="PRODUCT")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "NAME", nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "FK_SUPPLIER", nullable = false)
	private Supplier supplier;

	@ManyToOne
	@JoinColumn(name = "FK_CATEGORY", nullable = false)
	private Category category;

	@Column(name = "QUANTITY_AVAILABLE", nullable = false)
	private Integer quantityAvailable;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		createdAt = LocalDateTime.now();
	}

	public static Product of(ProductRequest request,
							 Supplier supplier,
							 Category category) {
		return Product
				.builder()
				.name(request.getName())
				.quantityAvailable(request.getQuantityAvailable())
				.supplier(supplier)
				.category(category)
				.build();
	}

	public void updateStock(Integer quantity) {
		quantityAvailable = quantityAvailable - quantity;
	}
}
