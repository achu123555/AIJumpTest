package com.achu.aijumptest.service.impl;

import com.achu.aijumptest.dto.CategoryQuestionDTO;
import com.achu.aijumptest.entity.Category;
import com.achu.aijumptest.mapper.CategoryMapper;
import com.achu.aijumptest.mapper.QuestionMapper;
import com.achu.aijumptest.service.CategoryService;
import com.achu.aijumptest.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<CategoryVO> categoryVOList = categoryListToCategoryVOList(categories);
        //2.填充题目数量
        getAndFillCategoryCount(categoryVOList);
        //3.返回分类VO
        return categoryVOList;
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        //1.查询所有分类 Entity转成VO对象
        List<Category> categories = list();
        List<CategoryVO> categoryVOList = categoryListToCategoryVOList(categories);
        //2.查询题目数量并批量填充题目数量字段
        getAndFillCategoryCount(categoryVOList);
        //3.平铺结构 转成 树状结构
        //3.1 先根据parentId分组 [原集合 → MapByParentId] 用于方便根据ID判断赋值
        Map<Long, List<CategoryVO>> MapByParentId = categoryVOList.stream()
                .collect(Collectors.groupingBy(CategoryVO::getParentId));
        //3.2 过滤出第一层父分类 [原集合 → categoryVOParentList] 填充children字段
        List<CategoryVO> categoryVOParentList = categoryVOList.stream()
                .filter(categoryVO -> categoryVO.getParentId() == 0)
                .toList();
        //3.3 批量赋值第一层父分类的children字段 形出树状结构 [用MapByParentId填充categoryVOParentList]
        categoryVOParentList
                .forEach(categoryVO -> {
                    //批量赋children字段
                    List<CategoryVO> children = MapByParentId.getOrDefault(categoryVO.getId(), new ArrayList<>());
                    categoryVO.setChildren(children);
                    //批量设置count = 当前count + 子类count
                    long sum = children.stream()
                            .mapToLong(CategoryVO::getQuestionCount)
                            .sum();
                    categoryVO.setQuestionCount(sum);
                });
        return categoryVOParentList;
    }

    /**
     * 查询并填充categoryVO集合中题目数量字段的方法
     * @param categoryVOList 题目分类集合 List<Map>
      */
    private void getAndFillCategoryCount(List<CategoryVO> categoryVOList){
        //1.先判断集合是否为空。[对一个数据或集合进行大量操作之前，先判断一下集合。]
        if (categoryVOList == null || categoryVOList.isEmpty()) {
            return;
        }
        //2.查询每个分类下的题目数量[mapper]
        List<CategoryQuestionDTO> countList = questionMapper.selectQuestionCount();
        // 3. 把 List<DTO> 转成 Map<Long, Long> 方便填充
        Map<Long, Long> resultCount = countList.stream()
                .collect(Collectors.toMap(
                        CategoryQuestionDTO::getCategoryId,
                        CategoryQuestionDTO::getCt
                ));
        //4.遍历分类 VO，根据分类 id 填充题目数量
        categoryVOList.forEach(
                categoryVO -> {
                    categoryVO.setQuestionCount(resultCount.getOrDefault(categoryVO.getId(),0L));
                }
        );
    }

    /**
     * category集合 → categoryVO集合
     * @param categories 传入的category集合
     * @return 返回categoryVO集合
     */
    private List<CategoryVO> categoryListToCategoryVOList(List<Category> categories){
        return categories.stream()
                .map(category -> {
                    CategoryVO categoryVO = new CategoryVO();
                    categoryVO.setId(category.getId());
                    categoryVO.setParentId(category.getParentId());
                    categoryVO.setName(category.getName());
                    categoryVO.setSort(category.getSort());
                    categoryVO.setCreateTime(category.getCreateTime());
                    categoryVO.setUpdateTime(category.getUpdateTime());
                    return categoryVO;
                })
                .toList();
    }

}
