package com.achu.aijumptest.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.achu.aijumptest.dto.PaperDTO;
import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.entity.PaperQuestion;
import com.achu.aijumptest.exception.BusinessException;
import com.achu.aijumptest.mapper.PaperMapper;
import com.achu.aijumptest.mapper.PaperQuestionMapper;
import com.achu.aijumptest.service.PaperService;
import com.achu.aijumptest.vo.PaperVO;
import com.achu.aijumptest.vo.QuestionDetailVO;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * projectName: com.achu.aijumptest.service.impl.PaperServiceImpl
 *
 * @author: achu_code
 * description: 试卷业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional
    @Override
    public Paper createPaper(PaperDTO.Save save) {
        //1.先保存paper对象获取回显id

        //情况一：没有题目
        Paper paper = BeanUtil.copyProperties(save, Paper.class);
        paper.setStatus("DRAFT");
        if(ObjectUtils.isEmpty(save.getQuestions())){
            //没有选择题目
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            log.warn("本次{}组卷,没有选择题目！注意没有题目的试卷不能进行考试！",paper);
            baseMapper.insert(paper);
            return paper;
        }
        //情况二：有题目 → set题目 数量和总分
        paper.setQuestionCount(save.getQuestions().size());
        paper.setTotalScore(save.getQuestions()
                .values()
                .stream()
                .reduce(BigDecimal.ZERO,BigDecimal::add)
        );
        baseMapper.insert(paper);
        //2.得到paper回显的id 开始插入中间表
        List<PaperQuestion> paperQuestions = save.getQuestions()
                .entrySet()
                .stream()
                .map(entry -> {
                    PaperQuestion paperQuestion = new PaperQuestion();
                    paperQuestion.setPaperId(paper.getId());
                    paperQuestion.setQuestionId(Long.valueOf(entry.getKey()));
                    paperQuestion.setScore(entry.getKey().doubleValue());
                    return paperQuestion;
                }).toList();
        paperQuestionMapper.insert(paperQuestions);
        return paper;
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
