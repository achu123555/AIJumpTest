package com.achu.aijumptest.listener;

import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.excel.QuestionImportExcel;
import com.achu.aijumptest.service.QuestionService;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.listener.QuestionListener
 *
 * @author: achu_code
 * description: 题目 Excel 批量导入监听器。
 *
 * 说明：
 * 1. EasyExcel 每读取一行，就会回调 invoke 方法。
 * 2. 这里不要每读取一行就写一次数据库，否则性能差。
 * 3. 使用 cacheList 缓存数据，达到 BATCH_COUNT 后批量入库。
 */
@Slf4j
@RequiredArgsConstructor
public class QuestionListener implements ReadListener<QuestionImportExcel> {

    /**
     * 题目业务层。
     * 注意：Listener 不是 Spring 容器管理的 Bean，所以需要通过构造方法手动传入。
     */
    private final QuestionService questionService;

    /**
     * 每批次入库数量。数据量大时可以设置为 500 / 1000。
     */
    private static final int BATCH_COUNT = 500;

    /**
     * 缓存已经解析出来但还没有写入数据库的数据。
     */
    private final List<QuestionImportExcel> cacheList = new ArrayList<>(BATCH_COUNT);

    /**
     * 每解析一行 Excel 数据都会执行一次。
     *
     * @param data            当前行数据
     * @param analysisContext EasyExcel 解析上下文，可获取当前行号等信息
     */
    @Override
    public void invoke(QuestionImportExcel data, AnalysisContext analysisContext) {
        // Excel 行号是从 1 开始展示给用户看的；EasyExcel 的 rowIndex 是从 0 开始，所以这里 + 1。
        int rowNumber = analysisContext.readRowHolder().getRowIndex() + 1;

        // 基础判空。更完整的字段校验会放到 Service 层统一处理。
        if (data == null || data.getTitle() == null || data.getTitle().trim().isEmpty()) {
            throw new BusinessException("第 " + rowNumber + " 行题目不能为空，请检查 Excel 数据！");
        }

        cacheList.add(data);

        // 达到批量阈值后，执行一次数据库批量保存，防止一次性把所有数据放进内存导致 OOM。
        if (cacheList.size() >= BATCH_COUNT) {
            saveData();
        }
    }

    /**
     * 所有行读取完成后会执行一次。
     * 这里要处理最后不足 BATCH_COUNT 的尾批次数据。
     *
     * @param analysisContext EasyExcel 解析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cacheList.isEmpty()) {
            saveData();
        }
        log.info("题目 Excel 解析完成，批量导入结束！");
    }

    /**
     * 批量保存缓存数据。
     */
    private void saveData() {
        log.info("开始批量保存题目 Excel 数据，本批数量：{}", cacheList.size());

        // 注意：这里传入新集合，避免后续 clear 影响业务层处理。
        questionService.saveBatchFromExcel(new ArrayList<>(cacheList));

        // 入库完成后清空缓存，等待下一批数据。
        cacheList.clear();
    }
}
