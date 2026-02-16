package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ecommerce.v1.dto.RequestDto.UpdateCategoryRequest;
import org.ecommerce.v1.dto.ResponseDto.CategoryDTO;
import org.ecommerce.v1.entity.Category;
import org.ecommerce.v1.repository.CategoryRepository;
import org.ecommerce.v1.utils.exceptions.NotFoundException;


@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO createCategory(UpdateCategoryRequest request) {
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());

        categoryRepository.save(category);

        return convertToDto(category);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "#categoryId")
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return convertToDto(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategoriesPaged(
            int page,
            int size,
            String sortBy,
            String direction,
            String nameFilter
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categories;

        if (nameFilter != null && !nameFilter.isBlank()) {
            categories = categoryRepository.findByCategoryNameContainingIgnoreCase(nameFilter, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }

        return categories.map(this::convertToDto);
    }

    @CacheEvict(value = "categories", key = "#categoryId")
    public CategoryDTO updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (request.getCategoryName() != null) category.setCategoryName(request.getCategoryName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());

        return convertToDto(category);
    }

    @CacheEvict(value = "categories", key = "#categoryId")
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
