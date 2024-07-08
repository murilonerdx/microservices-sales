package org.murilonerdx.productms.modules.supplier.dto;

import lombok.Data;
import org.murilonerdx.productms.modules.supplier.model.Supplier;
import org.springframework.beans.BeanUtils;

@Data
public class SupplierResponse {

	private Integer id;
	private String name;

	public static SupplierResponse of(Supplier supplier) {
		var response = new SupplierResponse();
		BeanUtils.copyProperties(supplier, response);
		return response;
	}
}
