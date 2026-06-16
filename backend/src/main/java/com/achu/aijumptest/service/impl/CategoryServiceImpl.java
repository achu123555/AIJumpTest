package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Category;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.CategoryMapper;
import com.achu.aijumptest.mapper.QuestionMapper;
import com.achu.aijumptest.service.CategoryService;
import com.achu.aijumptest.vo.CategoryTreeVO;
import com.achu.aijumptest.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * projectName: com.achu.aijumptest.service.impl.CategoryServiceImpl
 *
 * @author: achu_code
 * description: 题目分类业务层实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<CategoryVO> getCategories() {
        //目标：查询所有题目分类，然后给每个分类补上“该分类下有多少题目”。

        //1.查出所有题目分类并Entity转化成VO对象
        List<Category> categories = list();
        List<CategoryVO> categoryVOList = toVOlist(categories,CategoryVO::new);
        //2.填充题目数量
        fillCategoryCount(categoryVOList);
        //3.返回分类VO
        return categoryVOList;
    }

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        //1.查询所有分类 Entity转成VO对象
        List<Category> categories = list();
        List<CategoryTreeVO> treeVOS = toVOlist(categories,CategoryTreeVO::new);
        //2.查询题目数量并批量填充题目数量字段
        fillCategoryCount(treeVOS);
        //3.平铺结构 转成 树状结构
        //3.1 先根据parentId分组 [原集合 → MapByParentId] 用于方便根据ID判断赋值
        Map<Long, List<CategoryTreeVO>> MapByParentId = treeVOS.stream()
                .collect(Collectors.groupingBy(CategoryTreeVO::getParentId));
        //3.2 过滤出第一层父分类 [原集合 → categoryVOParentList] 填充children字段
        List<CategoryTreeVO> treeParentVOS = treeVOS.stream()
                .filter(c -> c.getParentId() == 0)
                .toList();
        //3.3 批量赋值第一层父分类的children字段 形出树状结构 [用MapByParentId填充categoryVOParentList]
        treeParentVOS
                .forEach(c -> {
                    //批量赋children字段
                    List<CategoryTreeVO> children = MapByParentId.getOrDefault(c.getId(), new ArrayList<>());
                    //正序排序 .reversed()倒序
                    children.sort(Comparator.comparingInt(CategoryVO::getSort));
                    c.setChildren(children);
                    //批量设置count = 当前count + 子类count
                    long sum = children.stream()
                            .mapToLong(CategoryVO::getQuestionCount)
                            .sum();
                    c.setQuestionCount(sum);
                });
        return treeParentVOS;
    }

    @Override
    public void saveCategory(Category category) {
        //1.校验父分类是否存在
        Category parentCategory = getById(category.getParentId());
        if(parentCategory == null){
            throw new BusinessException("父分类不存在！");
        }
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId,category.getParentId())
                .eq(Category::getName,category.getName());
        //2.判断同一父分类下是否有重名子分类
        long count = count(queryWrapper);
        if(count > 0){
            throw new BusinessException("新增子分类失败！【%s】父分类下已有名为【%s】的子分类！"
                    .formatted(parentCategory.getName(),category.getName())
            );
        }
        //3.通过校验,进行保存
        save(category);
    }

    @Override
    public void updateCategory(Category category) {
        //1.校验父分类是否存在
        Category parentCategory = getById(category.getParentId());
        if(parentCategory == null){
            throw new BusinessException("父分类不存在！");
        }
        //2.同一父分类下,不可以有不同子分类id但相同分类名。
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId,category.getParentId()) //同一父分类
                .ne(Category::getId,category.getId()) //不同id
                .eq(Category::getName,category.getName()); //同名
        //service可以通过方法接收baseMapper对象
        CategoryMapper categoryMapper = getBaseMapper();
        boolean exists = categoryMapper.exists(queryWrapper);
        if(exists){
            throw new BusinessException("编辑子分类失败！【%s】父分类下已有名为【%s】的子分类！"
                    .formatted(parentCategory.getName(),category.getName())
            );
        }
        //3.通过校验,执行更新
        boolean update = updateById(category);
        if (!update) {
            throw new BusinessException("更新分类失败！");
        }
    }

    @Override
    public void removeCategoryById(Long id) {
        //1.一级父分类不可删除
        Category category = getById(id);
        if(category == null){
            throw new BusinessException("指定删除的分类不存在！");
        }
        if(category.getParentId() == 0){
            throw new BusinessException("一级分类不可删除！");
        }
        //2.防御性编程：查找是否有题目关联到此分类
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getCategoryId,id);
        Long count = questionMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException("删除【%s】分类失败！,此分类已关联【%s】道题目。"
                    .formatted(category.getName(),count)
            );
        }
        //3.通过校验，删除分类
        removeById(id);
    }

    /**
     * 查询并填充categoryVO集合中题目数量字段的方法
     * @param VOList 题目分类集合 List<Map>
     */
    private void fillCategoryCount(List<? extends CategoryVO> VOList){
        //1.先判断集合是否为空。[对一个数据或集合进行大量操作之前，先判断一下集合。]
        if (VOList == null || VOList.isEmpty()) {
            return;
        }
        //2.查询每个分类下的题目数量[mapper]
        List<QuestionDTO.Count> countList = questionMapper.selectQuestionCount();
        // 3. 把 List<DTO> 转成 Map<Long, Long> 方便填充
        Map<Long, Long> resultCount = countList.stream()
                .collect(Collectors.toMap(
                        QuestionDTO.Count::getCategoryId,
                        QuestionDTO.Count::getCt
                ));
        //4.遍历分类 VO，根据分类 id 填充题目数量
        VOList.forEach(
                categoryVO -> {
                    categoryVO.setQuestionCount(resultCount.getOrDefault(categoryVO.getId(),0L));
                }
        );
    }

    /**
     * category集合 → categoryVO集合
     * category集合 → categoryTreeVO集合
     * @param categories 传入的category集合
     * @return 返回categoryVO/categoryTreeVO集合
     */
    private <T extends CategoryVO>List<T> toVOlist(List<Category> categories, Supplier<T> supplier){
        return categories.stream()
                .map(category -> {
                    T t = supplier.get();
                    BeanUtil.copyProperties(category,t);
                    return t;
                }).toList();
    }

}