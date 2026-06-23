package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.entity.Category
 *
 * @author: achu_code
 * description: 题目分类实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("categories")
public class Category extends BaseEntity<Long>{

    @Schema(description = "分类名")
    private String name;
    @Schema(description = "父分类主键")
    private Long parentId;
    @Schema(description = "排序字段")
    private Integer sort;

}
