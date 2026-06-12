package com.dengjiajia.finance.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.service.CategoryService;
import com.dengjiajia.finance.service.OcrService;
import com.dengjiajia.finance.utils.AiUtil;
import com.dengjiajia.finance.vo.OcrResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private final AiUtil aiUtil;
    private final CategoryService categoryService;

    @Override
    public OcrResultVO recognizeBill(String imageBase64, Long userId) {
        String prompt = """
                请识别这张图片中的消费信息，提取以下内容并以JSON格式返回：
                {
                  "amount": 金额数字（只要数字，不要符号）,
                  "category": 消费分类（从以下选择：餐饮、交通、购物、娱乐、学习、医疗、住房、通讯、运动、旅行、工资、兼职、其他支出、其他收入）,
                  "remark": 简短备注（10字以内）,
                  "type": 类型（1=支出，2=收入）
                }
                只返回JSON，不要其他文字。
                """;

        String result = aiUtil.chatWithImage(prompt, imageBase64);
        log.info("OCR识别结果: {}", result);
        return parseAiResult(result, userId);
    }

    @Override
    public OcrResultVO parseTextToBill(String text, Long userId) {
        String prompt = String.format("""
                用户输入了以下记账描述："%s"
                请提取消费信息并以JSON格式返回：
                {
                  "amount": 金额数字（只要数字，不要符号）,
                  "category": 消费分类（从以下选择：餐饮、交通、购物、娱乐、学习、医疗、住房、通讯、运动、旅行、工资、兼职、其他支出、其他收入）,
                  "remark": 简短备注（10字以内）,
                  "type": 类型（1=支出，2=收入）
                }
                只返回JSON，不要其他文字。
                """, text);

        String result = aiUtil.chat(prompt);
        log.info("AI解析结果: {}", result);
        return parseAiResult(result, userId);
    }

    private OcrResultVO parseAiResult(String aiResult, Long userId) {
        OcrResultVO vo = new OcrResultVO();
        vo.setRawText(aiResult);

        try {
            // 清理AI返回的markdown格式
            String json = aiResult
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JSONObject obj = JSON.parseObject(json);
            vo.setAmount(obj.getBigDecimal("amount"));
            vo.setType(obj.getInteger("type"));
            vo.setRemark(obj.getString("remark"));
            vo.setCategoryName(obj.getString("category"));

            // 匹配用户的分类ID
            String categoryName = obj.getString("category");
            List<Category> categories = categoryService.lambdaQuery()
                    .eq(Category::getUserId, userId)
                    .list();

            categories.stream()
                    .filter(c -> c.getName().contains(categoryName)
                            || categoryName.contains(c.getName()))
                    .findFirst()
                    .ifPresent(c -> vo.setCategoryId(c.getId()));

        } catch (Exception e) {
            log.error("解析AI结果失败: {}", e.getMessage());
            vo.setAmount(BigDecimal.ZERO);
            vo.setCategoryName("其他支出");
            vo.setType(1);
        }

        return vo;
    }
}

