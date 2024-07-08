package org.murilonerdx.productms.modules.category.controller;

import lombok.AllArgsConstructor;
import org.murilonerdx.productms.config.exception.SuccessResponse;
import org.murilonerdx.productms.modules.category.dto.CategoryRequest;
import org.murilonerdx.productms.modules.category.dto.CategoryResponse;
import org.murilonerdx.productms.modules.category.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/category")
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	public CategoryResponse save(@RequestBody CategoryRequest request) {
		return categoryService.save(request);
	}

	@GetMapping
	public List<CategoryResponse> findAll() {
		return categoryService.findAll();
	}

	@GetMapping("{id}")
	public CategoryResponse findById(@PathVariable Integer id) {
		return categoryService.findByIdResponse(id);
	}

	@GetMapping("description/{description}")
	public List<CategoryResponse> findByDescription(@PathVariable String description) {
		return categoryService.findByDescription(description);
	}

	@PutMapping("{id}")
	public CategoryResponse update(@RequestBody CategoryRequest request,
								   @PathVariable Integer id) {
		return categoryService.update(request, id);
	}

	@DeleteMapping("{id}")
	public SuccessResponse delete(@PathVariable Integer id) {
		return categoryService.delete(id);
	}
}
