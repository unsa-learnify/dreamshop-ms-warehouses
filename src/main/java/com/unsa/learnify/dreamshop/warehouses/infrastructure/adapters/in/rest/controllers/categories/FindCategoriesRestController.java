package com.unsa.learnify.dreamshop.warehouses.infrastructure.adapters.in.rest.controllers.categories;

import com.unsa.learnify.dreamshop.warehouses.application.ports.in.categories.FindCategoriesServicePort;
import com.unsa.learnify.dreamshop.warehouses.domain.models.Category;
import com.unsa.learnify.dreamshop.warehouses.domain.models.CategoryFilters;
import com.unsa.learnify.dreamshop.warehouses.domain.models.Page;
import com.unsa.learnify.dreamshop.warehouses.infrastructure.adapters.in.rest.dtos.categories.CategoryResponse;
import com.unsa.learnify.dreamshop.warehouses.infrastructure.adapters.in.rest.dtos.categories.CategoryPageRequest;
import com.unsa.learnify.dreamshop.warehouses.infrastructure.adapters.in.rest.mappers.CategoryRestMapper;
import com.unsa.learnify.dreamshop.warehouses.infrastructure.adapters.in.rest.utils.IntegerUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class FindCategoriesRestController {
    private final FindCategoriesServicePort findCategoriesServicePort;
    public FindCategoriesRestController(FindCategoriesServicePort findCategoriesServicePort) {
        this.findCategoriesServicePort = findCategoriesServicePort;
    }
    @Operation(
        summary = "Retrieve categories by page",
        description = "Fetches a paginated list of categories based on the provided filters.",
        tags = {"Categories"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "List of categories retrieved successfully.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema =  @Schema(implementation = CategoryResponse.class)))
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No categories found matching the provided filters."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid query parameters.",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findCategoriesByPage(@ModelAttribute @Valid CategoryPageRequest categoryPageRequest) {
        Integer page = IntegerUtils.safeParseInteger(categoryPageRequest.getPage(), 0);
        Integer size = IntegerUtils.safeParseInteger(categoryPageRequest.getSize(), 10);
        Page pageable = Page.builder().number(page).size(size).build();
        CategoryFilters categoryFilters = CategoryFilters.builder().page(pageable).name(categoryPageRequest.getName()).build();
        List<Category> categories = this.findCategoriesServicePort.execute(categoryFilters);
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<CategoryResponse> categoryResponses = categories
            .stream()
            .map(CategoryRestMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(categoryResponses);
    }
}
