package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.PaperMapper;
import com.achu.aijumptest.mapper.PaperQuestionMapper;
import com.achu.aijumptest.service.PaperService;
import com.achu.aijumptest.vo.PaperVO;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * projectName: com.achu.aijumptest.service.impl.PaperServiceImpl
 *
 * @author: achu_code
 * description: 试卷业务层实现类
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    private final PaperQuestionMapper paperQuestionMapper;

    @Override
    public PaperVO.Detail getDetailPaper(Long id) {
        //1.查询试卷
        Paper paper = baseMapper.selectById(id);
        if(paper == null){
            throw new BusinessException("查询不到id为%s的试卷".formatted(id));
        }
        PaperVO.Detail paperVo = BeanUtil.copyProperties(paper, PaperVO.Detail.class);
        //2.查询题目列表(题目+答案+选项)
        List<QuestionDetailVO> questions = paperQuestionMapper.selectQuestions(id);
        if(ObjectUtils.isEmpty(questions)){
            questions = new ArrayList<>();
            paperVo.setQuestions(questions);
            return paperVo;
        }

        //3.对题目进行排序 选择题 > 判断题 > 简答题
        List<QuestionDetailVO> voList = questions.stream()
                .sorted(new Comparator<QuestionDetailVO>() {
                    @Override
                    public int compare(QuestionDetailVO o1, QuestionDetailVO o2) {
                        return TypeToInt(o1.getType()) - TypeToInt(o2.getType());
                    }
                })
                .toList();
        paperVo.setQuestions(voList);
        return paperVo;

    }

    /**
     * 题目Type → String转int → 用于排序
     * @param type 题目类型
     * @return 排序数字
     */
    private int TypeToInt(String type){
        return switch (type) {
            case "CHOICE" -> 1;
            case "JUDGE" -> 2;
            case "TEXT" -> 3;
            default -> 4;
        };
    }

}
