package org.murilonerdx.productms.modules.product.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.murilonerdx.productms.config.exception.SuccessResponse;
import org.murilonerdx.productms.config.exception.ValidationException;
import org.murilonerdx.productms.modules.category.service.CategoryService;
import org.murilonerdx.productms.modules.product.dto.*;
import org.murilonerdx.productms.modules.product.model.Product;
import org.murilonerdx.productms.modules.product.repository.ProductRepository;
import org.murilonerdx.productms.modules.sales.client.SalesClient;
import org.murilonerdx.productms.modules.sales.dto.SalesConfirmationDTO;
import org.murilonerdx.productms.modules.sales.dto.SalesProductResponse;
import org.murilonerdx.productms.modules.sales.enums.SalesStatus;
import org.murilonerdx.productms.modules.sales.rabbitmq.SalesConfirmationSender;
import org.murilonerdx.productms.modules.supplier.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

	private static final Integer ZERO = 0;
	private static final String AUTHORIZATION = "Authorization";
	private static final String TRANSACTION_ID = "transactionid";
	private static final String SERVICE_ID = "serviceid";

	private final ProductRepository productRepository;
	private final SupplierService supplierService;
	private final CategoryService categoryService;
	private final SalesConfirmationSender salesConfirmationSender;
	private final SalesClient salesClient;
	private final ObjectMapper objectMapper;

	public ProductResponse save(ProductRequest request) {
		validateProductDataInformed(request);
		validateCategoryAndSupplierIdInformed(request);
		var category = categoryService.findById(request.getCategoryId());
		var supplier = supplierService.findById(request.getSupplierId());
		var product = productRepository.save(Product.of(request, supplier, category));
		return ProductResponse.of(product);
	}

	public ProductResponse update(ProductRequest request,
								  Integer id) {
		validateProductDataInformed(request);
		validateInformedId(id);
		validateCategoryAndSupplierIdInformed(request);
		var category = categoryService.findById(request.getCategoryId());
		var supplier = supplierService.findById(request.getSupplierId());
		var product = Product.of(request, supplier, category);
		product.setId(id);
		productRepository.save(product);
		return ProductResponse.of(product);
	}

	private void validateProductDataInformed(ProductRequest request) {
		if (isEmpty(request.getName())) {
			throw new ValidationException("The product's name was not informed.");
		}
		if (isEmpty(request.getQuantityAvailable())) {
			throw new ValidationException("The product's quantity was not informed.");
		}
		if (request.getQuantityAvailable() <= ZERO) {
			throw new ValidationException("The quantity should not be less or equal to zero.");
		}
	}

	private void validateCategoryAndSupplierIdInformed(ProductRequest request) {
		if (isEmpty(request.getCategoryId())) {
			throw new ValidationException("The category ID was not informed.");
		}
		if (isEmpty(request.getSupplierId())) {
			throw new ValidationException("The supplier ID was not informed.");
		}
	}

	public List<ProductResponse> findAll() {
		return productRepository
				.findAll()
				.stream()
				.map(ProductResponse::of)
				.collect(Collectors.toList());
	}

	public List<ProductResponse> findByName(String name) {
		if (isEmpty(name)) {
			throw new ValidationException("The product name must be informed.");
		}
		return productRepository
				.findByNameIgnoreCaseContaining(name)
				.stream()
				.map(ProductResponse::of)
				.collect(Collectors.toList());
	}

	public List<ProductResponse> findBySupplierId(Integer supplierId) {
		if (isEmpty(supplierId)) {
			throw new ValidationException("The product' supplier ID name must be informed.");
		}
		return productRepository
				.findBySupplierId(supplierId)
				.stream()
				.map(ProductResponse::of)
				.collect(Collectors.toList());
	}

	public List<ProductResponse> findByCategoryId(Integer categoryId) {
		if (isEmpty(categoryId)) {
			throw new ValidationException("The product' category ID name must be informed.");
		}
		return productRepository
				.findByCategoryId(categoryId)
				.stream()
				.map(ProductResponse::of)
				.collect(Collectors.toList());
	}

	public ProductResponse findByIdResponse(Integer id) {
		return ProductResponse.of(findById(id));
	}

	public Product findById(Integer id) {
		validateInformedId(id);
		return productRepository
				.findById(id)
				.orElseThrow(() -> new ValidationException("There's no product for the given ID."));
	}

	public Boolean existsByCategoryId(Integer categoryId) {
		return productRepository.existsByCategoryId(categoryId);
	}

	public Boolean existsBySupplierId(Integer supplierId) {
		return productRepository.existsBySupplierId(supplierId);
	}

	public SuccessResponse delete(Integer id) {
		validateInformedId(id);
		if (!productRepository.existsById(id)) {
			throw new ValidationException("The product does not exists.");
		}
		var sales = getSalesByProductId(id);
		if (!isEmpty(sales.getSalesIds())) {
			throw new ValidationException("The product cannot be deleted. There are sales for it.");
		}
		productRepository.deleteById(id);
		return SuccessResponse.create("The product was deleted.");
	}

	private void validateInformedId(Integer id) {
		if (isEmpty(id)) {
			throw new ValidationException("The supplier ID must be informed.");
		}
	}

	public void updateProductStock(ProductStockDTO product) {
		try {
			validateStockUpdateData(product);
			updateStock(product);
		} catch (Exception ex) {
			log.error("Error while trying to update stock for message with error: {}", ex.getMessage(), ex);
			var rejectedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.REJECTED, product.getTransactionid());
			salesConfirmationSender.sendSalesConfirmationMessage(rejectedMessage);
		}
	}

	private void validateStockUpdateData(ProductStockDTO product) {
		if (isEmpty(product)
				|| isEmpty(product.getSalesId())) {
			throw new ValidationException("The product data and the sales ID must be informed.");
		}
		if (isEmpty(product.getProducts())) {
			throw new ValidationException("The sales' products must be informed.");
		}
		product
				.getProducts()
				.forEach(salesProduct -> {
					if (isEmpty(salesProduct.getQuantity())
							|| isEmpty(salesProduct.getProductId())) {
						throw new ValidationException("The productID and the quantity must be informed.");
					}
				});
	}

	@Transactional
	protected void updateStock(ProductStockDTO product) {
		var productsForUpdate = new ArrayList<Product>();
		product
				.getProducts()
				.forEach(salesProduct -> {
					var existingProduct = findById(salesProduct.getProductId());
					validateQuantityInStock(salesProduct, existingProduct);
					existingProduct.updateStock(salesProduct.getQuantity());
					productsForUpdate.add(existingProduct);
				});
		if (!isEmpty(productsForUpdate)) {
			productRepository.saveAll(productsForUpdate);
			var approvedMessage = new SalesConfirmationDTO(product.getSalesId(), SalesStatus.APPROVED, product.getTransactionid());
			salesConfirmationSender.sendSalesConfirmationMessage(approvedMessage);
		}
	}

	private void validateQuantityInStock(ProductQuantityDTO salesProduct,
										 Product existingProduct) {
		if (salesProduct.getQuantity() > existingProduct.getQuantityAvailable()) {
			throw new ValidationException(
					String.format("The product %s is out of stock.", existingProduct.getId()));
		}
	}

	public ProductSalesResponse findProductSales(Integer id) {
		var product = findById(id);
		var sales = getSalesByProductId(product.getId());
		return ProductSalesResponse.of(product, sales.getSalesIds());
	}

	public static HttpServletRequest getCurrentRequest() {
		try {
			return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
					.getRequestAttributes()))
					.getRequest();
		} catch (Exception ex) {
			log.info("Current request could not be proccessed {}", ex.getMessage());
			throw new ValidationException("The current request could not be proccessed.");
		}
	}

	private SalesProductResponse getSalesByProductId(Integer productId) {
		try {
			var currentRequest = getCurrentRequest();
			var token = currentRequest.getHeader(AUTHORIZATION);
			var transactionid = currentRequest.getHeader(TRANSACTION_ID);
			var serviceid = currentRequest.getAttribute(SERVICE_ID);
			log.info("Sending GET request to orders by productId with data {} | [transactionID: {} | serviceID: {}]",
					productId, transactionid, serviceid);
			var response = salesClient
					.findSalesByProductId(productId, token, transactionid)
					.orElseThrow(() -> new ValidationException("The sales was not found by this product."));
			log.info("Recieving response from orders by productId with data {} | [transactionID: {} | serviceID: {}]",
					objectMapper.writeValueAsString(response), transactionid, serviceid);
			return response;
		} catch (Exception ex) {
			log.error("Error trying to call Sales-API: {}", ex.getMessage());
			throw new ValidationException("The sales could not be found.");
		}
	}

	public SuccessResponse checkProductsStock(ProductCheckStockRequest request) {
		try {
			var currentRequest = getCurrentRequest();
			var transactionid = currentRequest.getHeader(TRANSACTION_ID);
			var serviceid = currentRequest.getAttribute(SERVICE_ID);
			log.info("Request to POST product stock with data {} | [transactionID: {} | serviceID: {}]",
					objectMapper.writeValueAsString(request), transactionid, serviceid);
			if (isEmpty(request) || isEmpty(request.getProducts())) {
				throw new ValidationException("The request data and products must be informed.");
			}
			request
					.getProducts()
					.forEach(this::validateStock);
			var response = SuccessResponse.create("The stock is ok!");
			log.info("Response to POST product stock with data {} | [transactionID: {} | serviceID: {}]",
					objectMapper.writeValueAsString(response), transactionid, serviceid);
			return response;
		} catch (Exception ex) {
			throw new ValidationException(ex.getMessage());
		}
	}

	private void validateStock(ProductQuantityDTO productQuantity) {
		if (isEmpty(productQuantity.getProductId()) || isEmpty(productQuantity.getQuantity())) {
			throw new ValidationException("Product ID and quantity must be informed.");
		}
		var product = findById(productQuantity.getProductId());
		if (productQuantity.getQuantity() > product.getQuantityAvailable()) {
			throw new ValidationException(String.format("The product %s is out of stock.", product.getId()));
		}
	}
}
