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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public PaperVO.Detail getDetailPaper(Integer id) {
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
    public Paper createPaper(PaperDTO.Create create) {
        //1.先保存paper对象获取回显id

        //情况一：没有题目
        Paper paper = BeanUtil.copyProperties(create, Paper.class);
        paper.setStatus("DRAFT");
        if(ObjectUtils.isEmpty(create.getQuestions())){
            //没有选择题目
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            log.warn("本次{}组卷,没有选择题目！注意没有题目的试卷不能进行考试！",paper);
            baseMapper.insert(paper);
            return paper;
        }
        //情况二：有题目 → set题目 数量和总分
        paper.setQuestionCount(create.getQuestions().size());
        paper.setTotalScore(create.getQuestions()
                .values()
                .stream()
                .reduce(BigDecimal.ZERO,BigDecimal::add)
        );
        baseMapper.insert(paper);
        //2.得到paper回显的id 开始插入中间表
        List<PaperQuestion> paperQuestions = ToPaperQuestions(create, paper);
        paperQuestionMapper.insert(paperQuestions);
        return paper;
    }

    @Override
    public Paper intelligentCreatePaper(PaperDTO.IntelligentCreate create) {
        // 1.拷贝智能组卷相同参数到paper类,并设置初始状态
        Paper paper = BeanUtil.copyProperties(create, Paper.class);
        paper.setStatus("DRAFT");
        // 2.循环组卷规则
        //TODO
        return null;
    }

    @Transactional
    @Override
    public Paper update(Integer id, PaperDTO.Create update) {


        //1.基本校验（X不同id同名,X试卷状态为PUBLISHED[实际状态以在数据库中的试卷为准]）
        Paper paper = baseMapper.selectById(id);
        if(paper == null){
            throw new BusinessException("找不到该试卷！");
        }
        if("PUBLISHED".equals(paper.getStatus())){
            throw new BusinessException("该试卷正处于发布状态,不可编辑！");
        }
        boolean exists = baseMapper.exists(
                new LambdaQueryWrapper<Paper>()
                        .ne(Paper::getId, paper.getId())
                        .eq(Paper::getName, update.getName())
        );
        if(exists){
            throw new BusinessException("该试卷名称已存在,请勿重复提交！");
        }
        BeanUtil.copyProperties(update,paper);

        //2.校验是否选择了题目
        boolean hasQuestions = !ObjectUtils.isEmpty(update.getQuestions());
        if (!hasQuestions) {
            //情况一：没有题目
            paper.setTotalScore(BigDecimal.ZERO);
            paper.setQuestionCount(0);
            log.warn("本次id为{}的试卷更新没有选择题目！", id);
        } else {
            //情况二：有题目,设置题目数量和总分数
            paper.setQuestionCount(update.getQuestions().size());
            paper.setTotalScore(update.getQuestions()
                    .values()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
        }
        // 3.更新试卷主表
        baseMapper.updateById(paper);

        // 4.清除该试卷原有的所有中间表关联记录
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, id)
        );

        // 5.如果有新题目，开始连环重建中间表
        if (hasQuestions) {
            List<PaperQuestion> paperQuestions = ToPaperQuestions(update, paper);
            paperQuestionMapper.insert(paperQuestions);
        }

        return paper;
    }

    private List<PaperQuestion> ToPaperQuestions(PaperDTO.Create update, Paper paper) {
        return update.getQuestions()
                .entrySet()
                .stream()
                .map(entry -> {
                    PaperQuestion paperQuestion = new PaperQuestion();
                    paperQuestion.setPaperId(paper.getId());
                    paperQuestion.setQuestionId(Long.valueOf(entry.getKey()));
                    paperQuestion.setScore(entry.getValue().doubleValue());
                    return paperQuestion;
                }).toList();
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
