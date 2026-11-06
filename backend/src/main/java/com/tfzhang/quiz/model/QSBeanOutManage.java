package com.tfzhang.quiz.model;

import java.util.List;

// 保留 ID 供编辑/删除，answer 为答案文本，answerKey 为 a/b/c/d。
public record QSBeanOutManage(Integer id, String questionText, List<String> options, String answer, String answerKey) { }
