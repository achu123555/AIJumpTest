package com.achu.aijumptest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.vo.CategoryVO
 *
 * @author: achu_code
 * description: category数据传输对象
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "题目分类展示对象")
public class CategoryVO extends BaseVO {

    @Schema(description = "分类名")
    private String name;
    @Schema(description = "父分类主键")
    private Long parentId;
    @Schema(description = "排序字段")
    private Integer sort;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children = new ArrayList<>();
    @Schema(description = "该分类下的题目数量")
    private Long questionCount;

}
