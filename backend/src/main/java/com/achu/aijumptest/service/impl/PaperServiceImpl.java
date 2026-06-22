package com.achu.aijumptest.service.impl;

import com.achu.aijumptest.entity.Paper;
import com.achu.aijumptest.mapper.PaperMapper;
import com.achu.aijumptest.service.PaperService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * projectName: com.achu.aijumptest.service.impl.PaperServiceImpl
 *
 * @author: achu_code
 * description: 试卷业务层实现类
 */
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {
}
