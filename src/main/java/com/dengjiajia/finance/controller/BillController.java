package com.dengjiajia.finance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.BillDTO;
import com.dengjiajia.finance.dto.BillQueryDTO;
import com.dengjiajia.finance.service.BillService;
import com.dengjiajia.finance.vo.BillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
@Tag(name = "账单管理", description = "账单的增删改查和分页查询")
public class BillController {

    private final BillService billService;

    @PostMapping
    @Operation(summary = "添加账单", description = "创建新的账单记录")
    public Result<Void> add(@RequestBody BillDTO dto) {
        billService.add(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新账单", description = "更新已有的账单信息")
    public Result<Void> update(@RequestBody BillDTO dto) {
        billService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除账单", description = "根据ID删除账单记录")
    public Result<Void> delete(@Parameter(description = "账单ID") @PathVariable Long id) {
        billService.delete(id);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询账单", description = "根据条件分页查询账单列表")
    public Result<Page<BillVO>> page(BillQueryDTO query) {
        return Result.success(billService.page(query));
    }
}
