package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.QuestionDTO;
import com.achu.aijumptest.entity.Question;
import com.achu.aijumptest.excel.QuestionExportExcel;
import com.achu.aijumptest.excel.QuestionImportExcel;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.QuestionService
 *
 * @author: achu_code
 * description: 题目业务层
 */
public interface QuestionService extends IService<Question> {

    /**
     * 分页查询题目 1+N
     *
     * @param queryDTO 分页参数和查询条件
     * @return 分页查询结果
     */
    Page<QuestionDetailVO> queryByPage(QuestionDTO.Query queryDTO);

    /**
     * 增强分页查询：Java 层面操作，把数据拉到内存中处理 1+1+1
     *
     * @param queryDTO 分页参数和查询条件
     * @return 分页查询结果
     */
    Page<QuestionDetailVO> queryByPageEnhance(QuestionDTO.Query queryDTO);

    /**
     * 保存题目
     *
     * @param saveAndUpdateDTO 题目传输数据对象
     */
    void save(QuestionDTO.SaveAndUpdate saveAndUpdateDTO);

    /**
     * 根据 id 获取题目的详情信息，包括答案和选项
     *
     * @param id 题目 id
     * @return 题目展示对象
     */
    QuestionDetailVO getById(Long id);

    /**
     * 更新题目的详情信息接口，包括答案和选项
     *
     * @param updateDTO 更新数据传输 DTO
     */
    void update(QuestionDTO.SaveAndUpdate updateDTO);

    /**
     * 根据传入的 size 查找 size 条热门题目
     *
     * @param size 查找的热门题目数量
     * @return 完整题目列表
     */
    List<QuestionDetailVO> getPopularQuestion(Integer size);

    /**
     * 根据题目 id 删除题目及其选项与答案
     *
     * @param id 题目 id
     */
    void delete(Long id);

    /**
     * 批量导入题目 Excel。
     *
     * @param file 前端上传的 Excel 文件
     */
    void importQuestionExcel(MultipartFile file);

    /**
     * Listener 每解析一批 Excel 数据后，调用此方法批量入库。
     *
     * @param excelList Excel 行数据集合
     */
    void saveBatchFromExcel(List<QuestionImportExcel> excelList);

    /**
     * 根据查询条件导出题目 Excel。
     *
     * @param queryDTO 查询条件，导出时只使用 type、difficulty、categoryId、categoryIds、keyword 等筛选项
     * @return Excel 导出行数据
     */
    List<QuestionExportExcel> listQuestionExport(QuestionDTO.Query queryDTO);
}
