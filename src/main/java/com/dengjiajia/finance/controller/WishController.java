package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.WishDTO;
import com.dengjiajia.finance.service.WishService;
import com.dengjiajia.finance.vo.WishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/wish")
@RequiredArgsConstructor
@Tag(name = "愿望清单", description = "愿望清单的增删改查和存储功能")
public class WishController {

    private final WishService wishService;

    @PostMapping
    @Operation(summary = "添加愿望", description = "创建新的愿望清单项")
    public Result<Void> add(@RequestBody WishDTO dto) {
        wishService.add(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新愿望", description = "更新已有的愿望清单项")
    public Result<Void> update(@RequestBody WishDTO dto) {
        wishService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除愿望", description = "根据ID删除愿望清单项")
    public Result<Void> delete(@Parameter(description = "愿望ID") @PathVariable Long id) {
        wishService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取愿望列表", description = "获取所有愿望清单项")
    public Result<List<WishVO>> list() {
        return Result.success(wishService.listVO());
    }

    @PutMapping("/save/{id}")
    @Operation(summary = "存储金额", description = "为指定愿望存储金额")
    public Result<Void> saveAmount(
            @Parameter(description = "愿望ID") @PathVariable Long id,
            @Parameter(description = "存储金额") @RequestParam BigDecimal amount) {
        wishService.saveAmount(id, amount);
        return Result.success();
    }
}

