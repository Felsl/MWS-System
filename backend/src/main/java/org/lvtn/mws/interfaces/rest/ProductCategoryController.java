package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.productcategory.*;
import org.lvtn.mws.interfaces.dto.request.productcategory.ProductCategoryRequest;
import org.lvtn.mws.interfaces.dto.response.productcategory.ProductCategoryResponse;
import org.lvtn.mws.interfaces.mapper.ProductCategoryWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final CreateProductCategoryUseCase createUseCase;
    private final GetAllProductCategoriesUseCase getAllUseCase;
    private final GetProductCategoryByIdUseCase getByIdUseCase;
    private final UpdateProductCategoryUseCase updateUseCase;
    private final DeleteProductCategoryUseCase deleteUseCase;
    private final ProductCategoryWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryResponse create(@Valid @RequestBody ProductCategoryRequest req) {
        return mapper.toResponse(createUseCase.execute(req.getCode(), req.getName(), req.getDescription()));
    }

    @GetMapping
    public List<ProductCategoryResponse> getAll() {
        return mapper.toResponseList(getAllUseCase.execute());
    }

    @GetMapping("/{id}")
    public ProductCategoryResponse getById(@PathVariable String id) {
        return mapper.toResponse(getByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ProductCategoryResponse update(@PathVariable String id,
                                          @Valid @RequestBody ProductCategoryRequest req) {
        return mapper.toResponse(updateUseCase.execute(id, req.getCode(), req.getName(), req.getDescription()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }
}
