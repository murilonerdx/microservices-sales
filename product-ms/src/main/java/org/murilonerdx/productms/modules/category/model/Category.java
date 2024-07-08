package org.murilonerdx.productms.modules.category.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.murilonerdx.productms.modules.category.dto.CategoryRequest;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name= "CATEGORY")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;
	private String description;


	public static Category of(CategoryRequest request) {
		var category = new Category();
		BeanUtils.copyProperties(request, category);
		return category;
	}
}
