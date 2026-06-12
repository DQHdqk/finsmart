package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.BudgetDTO;
import com.dengjiajia.finance.service.BudgetService;
import com.dengjiajia.finance.vo.BudgetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public Result<Void> saveOrUpdate(@RequestBody BudgetDTO dto) {
        budgetService.saveOrUpdate(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<BudgetVO>> list(@RequestParam String month) {
        return Result.success(budgetService.list(month));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return Result.success();
    }
}

