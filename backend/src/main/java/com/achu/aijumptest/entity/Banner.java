package com.achu.aijumptest.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * projectName: com.achu.aijumptest.entity.Banner
 *
 * @author: achu_code
 * description: 轮播图实体类
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("banners")
public class Banner extends BaseEntity<Long>{
    @Schema(description = "轮播图标题")
    private String title;
    @Schema(description = "轮播图描述")
    private String description;
    @Schema(description = "轮播图URL地址")
    private String imageUrl;
    @Schema(description = "轮播图跳转地址")
    private String linkUrl;
    @Schema(description = "轮播图排序字段")
    private Integer sortOrder;
    @Schema(description = "轮播图激活状态")
    private Boolean isActive;
}
