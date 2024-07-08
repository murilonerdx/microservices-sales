package org.murilonerdx.productms.modules.category.dto;

import lombok.Data;
import org.murilonerdx.productms.modules.category.model.Category;
import org.springframework.beans.BeanUtils;

@Data
public class CategoryResponse {

	private Integer id;
	private String description;

	public static CategoryResponse of(Category category) {
		var response = new CategoryResponse();
		BeanUtils.copyProperties(category,response);
		return response;
	}
}
