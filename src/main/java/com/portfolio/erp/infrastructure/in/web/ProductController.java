package com.portfolio.erp.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.UnitOfMeasure;
import com.portfolio.erp.domain.ports.in.ProductUseCase;
import com.portfolio.erp.infrastructure.in.web.api.ProductsApi;
import com.portfolio.erp.infrastructure.in.web.dto.PageMetadata;
import com.portfolio.erp.infrastructure.in.web.dto.PageProduct;
import com.portfolio.erp.infrastructure.in.web.dto.ProductRequest;
import com.portfolio.erp.infrastructure.in.web.dto.ProductResponse;
import com.portfolio.erp.infrastructure.out.mapper.ProductMapper;

@RestController
public class ProductController implements ProductsApi {

    private final ProductUseCase productUseCase;
    private final ProductMapper productMapper;

    public ProductController(ProductUseCase productUseCase, ProductMapper productMapper) {
        this.productUseCase = productUseCase;
        this.productMapper = productMapper;
    }

    @Override
    public ResponseEntity<PageProduct> listProducts(Integer page, Integer size, String search,
                                                    Long categoryId, Boolean active) {
        PageResult<Product> result = productUseCase.list(search, categoryId, active,
                page == null ? 0 : page, size == null ? 20 : size);

        PageMetadata metadata = new PageMetadata();
        metadata.setNumber(result.number());
        metadata.setSize(result.size());
        metadata.setTotalElements(result.totalElements());
        metadata.setTotalPages(result.totalPages());

        PageProduct response = new PageProduct();
        response.setContent(result.content().stream().map(productMapper::toResponse).toList());
        response.setPage(metadata);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest) {
        Product created = productUseCase.create(toCommand(productRequest));
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.getId())
                        .toUri())
                .body(productMapper.toResponse(created));
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(Long id) {
        return ResponseEntity.ok(productMapper.toResponse(productUseCase.get(id)));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
        return ResponseEntity.ok(productMapper.toResponse(productUseCase.update(id, toCommand(productRequest))));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> deactivateProduct(Long id) {
        productUseCase.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private ProductUseCase.ProductCommand toCommand(ProductRequest request) {
        return new ProductUseCase.ProductCommand(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getCategoryId(),
                request.getUnit() == null ? null : UnitOfMeasure.valueOf(request.getUnit().getValue()),
                request.getSalePrice(),
                request.getCostPrice(),
                request.getTaxRate(),
                !Boolean.FALSE.equals(request.getActive()));
    }
}
