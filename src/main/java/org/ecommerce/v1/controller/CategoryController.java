package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.UpdateCategoryRequest;
import org.ecommerce.v1.dto.ResponseDto.CategoryDTO;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@AllArgsConstructor
@Tag(name = "Categories", description = "Category management")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<SuccessResponse<CategoryDTO>> createCategory(@RequestBody UpdateCategoryRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        SuccessResponse<CategoryDTO> res = new SuccessResponse<>("Category added successfully", category);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedResponse<CategoryDTO>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nameFilter
    ) {
        Page<CategoryDTO> categories = categoryService.getAllCategoriesPaged(page, size, sortBy, sortDir, nameFilter);

        PagedResponse<CategoryDTO> pagedResponse = new PagedResponse<>(
                categories.getContent(),
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalElements(),
                categories.getTotalPages()
        );

        SuccessResponse<PagedResponse<CategoryDTO>> res = new SuccessResponse<>("Categories fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        SuccessResponse<CategoryDTO> res = new SuccessResponse<>("Category retrieved successfully", category);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request
    ) {
        CategoryDTO category = categoryService.updateCategory(id, request);
        SuccessResponse<CategoryDTO> res = new SuccessResponse<>("Category updated successfully", category);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        SuccessResponse<String> res = new SuccessResponse<>("Category deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
