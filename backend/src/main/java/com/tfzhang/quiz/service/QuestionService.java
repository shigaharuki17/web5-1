package com.tfzhang.quiz.service;

import com.tfzhang.quiz.model.*;
import java.util.List;

public interface QuestionService {
    QSBeanOutManage insertQuestion(QSBean question);
    QSBeanOutManage updateQuestion(Integer id, QSBean question);
    boolean delQuestion(Integer id);
    QSBeanPage page(Integer page, Integer pageSize, String keyword);
    List<QSBeanOutManage> findByName(String keyword);
    List<QSBeanOut> getQuestions();
}
