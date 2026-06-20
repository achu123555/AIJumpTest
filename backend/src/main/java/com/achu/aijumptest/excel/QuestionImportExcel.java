package com.achu.aijumptest.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
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

/**
 * projectName: com.achu.aijumptest.excel.QuestionImportExcel
 *
 * @author: achu_code
 * description: 题目 Excel 批量导入映射实体类，也可以直接作为“题目导入模板”的表头类。
 *
 * 说明：
 * 1. 这个类对应 Excel 的一行数据。
 * 2. 不建议直接复用数据库实体 Question，因为 Excel 一行里还包含选项、答案、关键词等跨表字段。
 * 3. 用户填写 Excel 时，题目类型建议填：CHOICE / JUDGE / TEXT，也支持：选择题 / 判断题 / 简答题。
 * 4. 难度建议填：EASY / MEDIUM / HARD，也支持：简单 / 中等 / 困难。
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
public class QuestionImportExcel {

    /**
     * 题目标题，对应 questions.title。
     */
    @ExcelProperty(value = "题目", index = 0)
    @ColumnWidth(50)
    private String title;

    /**
     * 题目类型，对应 questions.type。
     * 可填：CHOICE / JUDGE / TEXT，或中文：选择题 / 判断题 / 简答题。
     */
    @ExcelProperty(value = "题目类型", index = 1)
    @ColumnWidth(16)
    private String type;

    /**
     * 是否多选，对应 questions.multi。
     * 只对选择题生效；可填：是/否、true/false、1/0、单选/多选。
     */
    @ExcelProperty(value = "是否多选", index = 2)
    @ColumnWidth(14)
    private String multi;

    /**
     * 分类 ID，对应 questions.category_id。
     * 例如：4-Java基础，5-Spring框架，6-Java核心技术，7-MySQL数据库，8-Redis分布式缓存。
     */
    @ExcelProperty(value = "分类ID", index = 3)
    @ColumnWidth(12)
    private Long categoryId;

    /**
     * 难度，对应 questions.difficulty。
     * 可填：EASY / MEDIUM / HARD，或中文：简单 / 中等 / 困难。
     */
    @ExcelProperty(value = "难度", index = 4)
    @ColumnWidth(14)
    private String difficulty;

    /**
     * 默认分值，对应 questions.score。
     */
    @ExcelProperty(value = "分值", index = 5)
    @ColumnWidth(10)
    private Integer score;

    /**
     * 选择题选项 A，对应 question_choices.content。
     * 判断题、简答题可以不填。
     */
    @ExcelProperty(value = "选项A", index = 6)
    @ColumnWidth(40)
    private String choiceA;

    /**
     * 选择题选项 B，对应 question_choices.content。
     */
    @ExcelProperty(value = "选项B", index = 7)
    @ColumnWidth(40)
    private String choiceB;

    /**
     * 选择题选项 C，对应 question_choices.content。
     */
    @ExcelProperty(value = "选项C", index = 8)
    @ColumnWidth(40)
    private String choiceC;

    /**
     * 选择题选项 D，对应 question_choices.content。
     */
    @ExcelProperty(value = "选项D", index = 9)
    @ColumnWidth(40)
    private String choiceD;

    /**
     * 正确答案，对应 question_answer.answer。
     * 选择题：A 或 A,C,D
     * 判断题：TRUE/FALSE，或 正确/错误、对/错
     * 简答题：标准答案文本
     */
    @ExcelProperty(value = "正确答案", index = 10)
    @ColumnWidth(30)
    private String answer;

    /**
     * 关键词，对应 question_answer.keywords。
     * 主要用于判断题、简答题的答案匹配；选择题可以不填。
     */
    @ExcelProperty(value = "关键词", index = 11)
    @ColumnWidth(35)
    private String keywords;

    /**
     * 题目解析，对应 questions.analysis。
     */
    @ExcelProperty(value = "解析", index = 12)
    @ColumnWidth(60)
    private String analysis;
}
