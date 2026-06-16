package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.product.*;
import org.lvtn.mws.interfaces.dto.request.product.ProductRequest;
import org.lvtn.mws.interfaces.dto.response.product.ProductResponse;
import org.lvtn.mws.interfaces.mapper.ProductWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createUseCase;
    private final GetProductByIdUseCase getByIdUseCase;
    private final GetAllProductsUseCase getAllUseCase;
    private final UpdateProductUseCase updateUseCase;
    private final DeleteProductUseCase deleteUseCase;
    private final SearchProductsUseCase searchUseCase;
    private final ProductWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        return mapper.toResponse(createUseCase.execute(
                req.getCategoryId(), req.getSku(), req.getBarcode(), req.getName(),
                req.getDescription(), req.getPrice(), req.getCostPrice(),
                req.getUnit(), req.getSafetyStock(), req.getWeight(),
                req.getVolume(), req.isHazardousFlag()));
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable String id) {
        return mapper.toResponse(getByIdUseCase.execute(id));
    }

    @GetMapping
    public List<ProductResponse> getAll(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return mapper.toResponseList(searchUseCase.execute(keyword));
        }
        return mapper.toResponseList(getAllUseCase.execute());
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable String id, @Valid @RequestBody ProductRequest req) {
        return mapper.toResponse(updateUseCase.execute(
                id, req.getCategoryId(), req.getSku(), req.getBarcode(), req.getName(),
                req.getDescription(), req.getPrice(), req.getCostPrice(),
                req.getUnit(), req.getSafetyStock(), req.getWeight(),
                req.getVolume(), req.isHazardousFlag()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }
}
