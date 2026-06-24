package com.achu.aijumptest.service;

import com.achu.aijumptest.dto.PaperDTO;
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
    PaperVO.Detail getDetailPaper(Integer id);

    /**
     * 手动组卷并保存
     *
     * @param create 要保存的试卷参数
     * @return 组装后保存的试卷
     */
    Paper createPaper(PaperDTO.Create create);

    /**
     * 智能组卷
     * @param create 智能组卷的参数
     * @return 组装后保存的试卷
     */
    Paper intelligentCreatePaper(PaperDTO.IntelligentCreate create);

    /**
     * 更新试卷
     * @param id 试卷id
     * @param update 更新的试卷DTO
     * @return 更新后的试卷
     */
    Paper update(Integer id, PaperDTO.Create update);
}
