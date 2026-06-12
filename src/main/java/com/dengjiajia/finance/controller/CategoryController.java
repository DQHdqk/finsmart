package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.CategoryDTO;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<Void> add(@RequestBody CategoryDTO dto) {
        categoryService.add(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CategoryDTO dto) {
        categoryService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.list(type));
    }
}

