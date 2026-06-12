package com.dengjiajia.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dengjiajia.finance.dto.CategoryDTO;
import com.dengjiajia.finance.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    void add(CategoryDTO dto);
    void update(CategoryDTO dto);
    void delete(Long id);
    List<Category> list(Integer type);
    void initDefault(Long userId);
}


