package com.achu.aijumptest.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.BooleanEnum;
import lombok.Data;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;

import java.time.LocalDateTime;

/**
 * projectName: com.achu.aijumptest.excel.QuestionExportExcel
 *
 * @author: achu_code
 * description: 题目 Excel 批量导出映射实体类。
 *
 * 说明：
 * 1. 导出类比导入类多了“题目ID”和“创建时间”，方便管理员核对数据。
 * 2. 这个类是一张扁平 Excel 表，内部会把 questions、question_choices、question_answer 三张表的数据合并到一行。
 */
@Data
@ExcelIgnoreUnannotated
@HeadRowHeight(26)
@ContentRowHeight(22)
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
@HeadFontStyle(fontHeightInPoints = 12, bold = BooleanEnum.TRUE)
@ContentStyle(
        horizontalAlignment = HorizontalAlignmentEnum.CENTER,
        verticalAlignment = VerticalAlignmentEnum.CENTER,
        wrapped = BooleanEnum.TRUE
)
@ContentFontStyle(fontHeightInPoints = 11)
public class QuestionExportExcel {

    /**
     * 题目主键 ID，对应 questions.id。
     */
    @ExcelProperty(value = "题目ID", index = 0)
    @ColumnWidth(12)
    private Long id;

    /**
     * 题目标题，对应 questions.title。
     */
    @ExcelProperty(value = "题目", index = 1)
    @ColumnWidth(50)
    private String title;

    /**
     * 题目类型，对应 questions.type。
     */
    @ExcelProperty(value = "题目类型", index = 2)
    @ColumnWidth(16)
    private String type;

    /**
     * 是否多选。为了 Excel 里更直观，这里导出为“是/否”。
     */
    @ExcelProperty(value = "是否多选", index = 3)
    @ColumnWidth(14)
    private String multi;

    /**
     * 分类 ID，对应 questions.category_id。
     */
    @ExcelProperty(value = "分类ID", index = 4)
    @ColumnWidth(12)
    private Long categoryId;

    /**
     * 难度，对应 questions.difficulty。
     */
    @ExcelProperty(value = "难度", index = 5)
    @ColumnWidth(14)
    private String difficulty;

    /**
     * 默认分值，对应 questions.score。
     */
    @ExcelProperty(value = "分值", index = 6)
    @ColumnWidth(10)
    private Integer score;

    /**
     * 选择题选项 A。
     */
    @ExcelProperty(value = "选项A", index = 7)
    @ColumnWidth(40)
    private String choiceA;

    /**
     * 选择题选项 B。
     */
    @ExcelProperty(value = "选项B", index = 8)
    @ColumnWidth(40)
    private String choiceB;

    /**
     * 选择题选项 C。
     */
    @ExcelProperty(value = "选项C", index = 9)
    @ColumnWidth(40)
    private String choiceC;

    /**
     * 选择题选项 D。
     */
    @ExcelProperty(value = "选项D", index = 10)
    @ColumnWidth(40)
    private String choiceD;

    /**
     * 正确答案，对应 question_answer.answer。
     */
    @ExcelProperty(value = "正确答案", index = 11)
    @ColumnWidth(30)
    private String answer;

    /**
     * 关键词，对应 question_answer.keywords。
     */
    @ExcelProperty(value = "关键词", index = 12)
    @ColumnWidth(35)
    private String keywords;

    /**
     * 题目解析，对应 questions.analysis。
     */
    @ExcelProperty(value = "解析", index = 13)
    @ColumnWidth(60)
    private String analysis;

    /**
     * 创建时间，对应 questions.create_time。
     */
    @ExcelProperty(value = "创建时间", index = 14)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(24)
    private LocalDateTime createTime;
}
