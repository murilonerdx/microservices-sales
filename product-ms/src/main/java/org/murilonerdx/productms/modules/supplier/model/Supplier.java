package org.murilonerdx.productms.modules.supplier.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.murilonerdx.productms.modules.supplier.dto.SupplierRequest;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "SUPPLIER")
public class Supplier {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;
	private String name;

	public static Supplier of(SupplierRequest request) {
		var supplier = new Supplier();
		BeanUtils.copyProperties(request, supplier);
		return supplier;
	}
}


