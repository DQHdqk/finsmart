package com.dengjiajia.finance.controller;

import com.dengjiajia.finance.common.Result;
import com.dengjiajia.finance.dto.MarkPaidDTO;
import com.dengjiajia.finance.dto.ShareBillDTO;
import com.dengjiajia.finance.service.ShareService;
import com.dengjiajia.finance.vo.ShareBillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/create")
    public Result<Void> create(@RequestBody ShareBillDTO dto) {
        shareService.create(dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<ShareBillVO>> list() {
        return Result.success(shareService.listVO());
    }

    @GetMapping("/detail/{id}")
    public Result<ShareBillVO> detail(@PathVariable Long id) {
        return Result.success(shareService.detail(id));
    }

    @PutMapping("/pay")
    public Result markPaid(@RequestBody MarkPaidDTO dto) {
        shareService.markPaid(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shareService.deleteBill(id);
        return Result.success();
    }
}
