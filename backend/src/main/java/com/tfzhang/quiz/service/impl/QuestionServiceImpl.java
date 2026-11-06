package com.tfzhang.quiz.service.impl;

import com.tfzhang.quiz.mapper.QuestionMapper;
import com.tfzhang.quiz.model.*;
import com.tfzhang.quiz.service.QuestionService;
import com.tfzhang.quiz.utils.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {
    private final QuestionMapper mapper;
    public QuestionServiceImpl(QuestionMapper mapper) { this.mapper = mapper; }

    @Override public QSBeanOutManage insertQuestion(QSBean input) {
        Question question = new Question();
        apply(input, question);
        question.setIsDelete(0);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(question.getCreateTime());
        if (mapper.insertQuestion(question) != 1 || question.getId() == null) throw new IllegalStateException("新增题目失败");
        return Tools.convertToQSBeanManageList(List.of(question)).get(0);
    }

    @Override public QSBeanOutManage updateQuestion(Integer id, QSBean input) {
        validateId(id);
        Question question = mapper.findById(id);
        if (question == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在或已被删除");
        apply(input, question);
        question.setUpdateTime(LocalDateTime.now());
        if (mapper.updateQuestion(question) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "题目不存在或已被删除");
        return Tools.convertToQSBeanManageList(List.of(question)).get(0);
    }

    @Override public boolean delQuestion(Integer id) { validateId(id); return mapper.delQuestionById(id) > 0; }

    @Override @Transactional(readOnly = true)
    public QSBeanPage page(Integer page, Integer pageSize, String keyword) {
        int offset = PageQuery.offset(page, pageSize);
        String search = PageQuery.like(keyword);
        return new QSBeanPage(mapper.count(search), Tools.convertToQSBeanManageList(mapper.page(offset, pageSize, search)));
    }

    @Override @Transactional(readOnly = true)
    public List<QSBeanOutManage> findByName(String keyword) { return Tools.convertToQSBeanManageList(mapper.findByName(PageQuery.like(keyword))); }

    @Override @Transactional(readOnly = true)
    public List<QSBeanOut> getQuestions() { return Tools.convertToQSBeanList(mapper.getQuestions()); }

    private void validateId(Integer id) { if (id == null || id <= 0) throw bad("题目 ID 必须为正整数"); }
    private ResponseStatusException bad(String text) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, text); }
    private String text(String value, String label) {
        if (value == null || value.isBlank()) throw bad(label + "不能为空");
        if (value.length() > 255) throw bad(label + "不能超过 255 个字符");
        return value.trim();
    }
    private void apply(QSBean input, Question target) {
        if (input == null) throw bad("题目内容不能为空");
        String title = text(input.getQuestion(), "题目");
        List<String> options = List.of(text(input.getOptiona(), "选项 A"), text(input.getOptionb(), "选项 B"), text(input.getOptionc(), "选项 C"), text(input.getOptiond(), "选项 D"));
        if (options.stream().map(s -> s.toLowerCase(Locale.ROOT)).distinct().count() != 4) throw bad("四个选项不能重复");
        String answer = input.getAnswer() == null ? "" : input.getAnswer().trim().toLowerCase(Locale.ROOT);
        if (!List.of("a", "b", "c", "d").contains(answer)) throw bad("正确答案必须为 A、B、C、D 中的一项");
        target.setQuestionText(title);
        target.setAnswer1Text(options.get(0)); target.setAnswer1Correct("a".equals(answer));
        target.setAnswer2Text(options.get(1)); target.setAnswer2Correct("b".equals(answer));
        target.setAnswer3Text(options.get(2)); target.setAnswer3Correct("c".equals(answer));
        target.setAnswer4Text(options.get(3)); target.setAnswer4Correct("d".equals(answer));
    }
}
