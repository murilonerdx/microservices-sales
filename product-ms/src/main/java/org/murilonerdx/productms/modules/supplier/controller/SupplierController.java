package org.murilonerdx.productms.modules.supplier.controller;

import lombok.AllArgsConstructor;
import org.murilonerdx.productms.config.exception.SuccessResponse;
import org.murilonerdx.productms.modules.supplier.dto.SupplierRequest;
import org.murilonerdx.productms.modules.supplier.dto.SupplierResponse;
import org.murilonerdx.productms.modules.supplier.service.SupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/supplier")
public class SupplierController {

	private final SupplierService supplierService;

	@PostMapping
	public SupplierResponse save(@RequestBody SupplierRequest request) {
		return supplierService.save(request);
	}

	@GetMapping
	public List<SupplierResponse> findAll() {
		return supplierService.findAll();
	}

	@GetMapping("{id}")
	public SupplierResponse findById(@PathVariable Integer id) {
		return supplierService.findByIdResponse(id);
	}

	@GetMapping("name/{name}")
	public List<SupplierResponse> findByName(@PathVariable String name) {
		return supplierService.findByName(name);
	}

	@PutMapping("{id}")
	public SupplierResponse update(@RequestBody SupplierRequest request,
								   @PathVariable Integer id) {
		return supplierService.update(request, id);
	}

	@DeleteMapping("{id}")
	public SuccessResponse delete(@PathVariable Integer id) {
		return supplierService.delete(id);
	}
}