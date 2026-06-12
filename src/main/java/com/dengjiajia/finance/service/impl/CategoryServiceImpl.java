package com.dengjiajia.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dengjiajia.finance.common.BusinessException;
import com.dengjiajia.finance.common.UserContext;
import com.dengjiajia.finance.dto.CategoryDTO;
import com.dengjiajia.finance.entity.Category;
import com.dengjiajia.finance.mapper.CategoryMapper;
import com.dengjiajia.finance.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Override
    public void add(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setUserId(UserContext.getUserId());
        this.save(category);
    }

    @Override
    public void update(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Category category = this.getById(dto.getId());
        if (category == null || !category.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("分类不存在");
        }
        BeanUtils.copyProperties(dto, category);
        this.updateById(category);
    }

    @Override
    public void delete(Long id) {
        Category category = this.getById(id);
        if (category == null || !category.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException("分类不存在");
        }
        this.removeById(id);
    }

    @Override
    public List<Category> list(Integer type) {
        return this.lambdaQuery()
                .eq(Category::getUserId, UserContext.getUserId())
                .eq(type != null, Category::getType, type)
                .orderByAsc(Category::getSort)
                .list();
    }

    @Override
    public void initDefault(Long userId) {
        // 注册时初始化默认分类
        List<String[]> defaults = Arrays.asList(
                new String[]{"餐饮", "1", "food", "#FF6B6B"},
                new String[]{"交通", "1", "bus", "#4ECDC4"},
                new String[]{"购物", "1", "shop", "#45B7D1"},
                new String[]{"娱乐", "1", "game", "#96CEB4"},
                new String[]{"学习", "1", "book", "#FFEAA7"},
                new String[]{"其他支出", "1", "other", "#DDA0DD"},
                new String[]{"工资", "2", "salary", "#98D8C8"},
                new String[]{"兼职", "2", "work", "#F7DC6F"},
                new String[]{"其他收入", "2", "gift", "#BB8FCE"}

        );
        for (int i = 0; i < defaults.size(); i++) {
            String[] d = defaults.get(i);
            Category category = new Category();
            category.setUserId(userId);
            category.setName(d[0]);
            category.setType(Integer.parseInt(d[1]));
            category.setIcon(d[2]);
            category.setColor(d[3]);
            category.setSort(i);
            this.save(category);
        }
    }
}

