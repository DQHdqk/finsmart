package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.SubscriptionDTO;
import com.dengjiajia.finance.service.SubscriptionService;
import com.dengjiajia.finance.vo.SubscriptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
@Tag(name = "订阅管理", description = "订阅服务的增删改查和提醒功能")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "添加订阅", description = "创建新的订阅服务记录")
    public Result<Void> add(@RequestBody SubscriptionDTO dto) {
        subscriptionService.add(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "更新订阅", description = "更新已有的订阅服务信息")
    public Result<Void> update(@RequestBody SubscriptionDTO dto) {
        subscriptionService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订阅", description = "根据ID删除订阅服务记录")
    public Result<Void> delete(@Parameter(description = "订阅ID") @PathVariable Long id) {
        subscriptionService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取订阅列表", description = "获取所有订阅服务列表")
    public Result<List<SubscriptionVO>> list() {
        return Result.success(subscriptionService.listVO());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "获取即将到期订阅", description = "获取即将到期的订阅服务列表")
    public Result<List<SubscriptionVO>> upcoming() {
        return Result.success(subscriptionService.upcoming());
    }
}

