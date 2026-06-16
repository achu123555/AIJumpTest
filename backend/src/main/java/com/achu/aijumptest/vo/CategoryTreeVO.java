package com.achu.aijumptest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.vo.CategoryTreeVO
 *
 * @author: achu_code
 * description: category分类树View Object
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryTreeVO extends CategoryVO{
    @Schema(description = "子分类树列表字段")
    private List<CategoryTreeVO> children;
}
