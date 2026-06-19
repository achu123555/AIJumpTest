package com.achu.aijumptest.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * projectName: com.achu.aijumptest.enums.QuestionType
 *
 * @author: achu_code
 * description: 题目类型枚举类
 */

@Getter
@AllArgsConstructor
public enum QuestionType {

    CHOICE("CHOICE"),
    JUDGE("JUDGE"),
    TEXT("TEXT");

    private final String Type;
}
