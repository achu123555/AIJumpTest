package com.achu.aijumptest.service;

import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.vo.PaperVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * projectName: com.achu.aijumptest.service.PaperService
 *
 * @author: achu_code
 * description: 试卷业务层接口
 */

public interface PaperService extends IService<Paper> {

    /**
     * 获取试卷的详情,包括题目和答案选项
     *
     * @return 试卷详情对象
     */
    PaperVO.Detail getDetailPaper(Long id);
}
