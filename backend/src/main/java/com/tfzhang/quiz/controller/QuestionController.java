package com.tfzhang.quiz.controller;

import com.tfzhang.quiz.model.*;
import com.tfzhang.quiz.service.QuestionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class QuestionController {
    private final QuestionService service;
    public QuestionController(QuestionService service) { this.service = service; }

    @PostMapping(value = {"/addQuestion", "/api/questions"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<QSBeanOutManage> add(@RequestBody QSBean question) { return Result.success("新增题目成功", service.insertQuestion(question)); }

    @PostMapping(value = "/addQuestion", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<QSBeanOutManage> addForm(@ModelAttribute QSBean question) { return add(question); }

    @PutMapping("/api/questions/{id}")
    public Result<QSBeanOutManage> update(@PathVariable Integer id, @RequestBody QSBean question) { return Result.success("题目信息已更新", service.updateQuestion(id, question)); }

    @GetMapping({"/questions", "/api/questions"})
    public Result<QSBeanPage> page(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "5") Integer pageSize,
                                 @RequestParam(required = false) String keyword) { return Result.success(service.page(page, pageSize, keyword)); }

    @GetMapping({"/findQuestion", "/api/questions/search"})
    public Result<List<QSBeanOutManage>> find(@RequestParam(defaultValue = "") String keyword) { return Result.success(service.findByName(keyword)); }

    @GetMapping({"/getQuestion", "/api/questions/random"})
    public Result<List<QSBeanOut>> random() { return Result.success(service.getQuestions()); }

    @DeleteMapping("/api/questions/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return service.delQuestion(id) ? Result.success("题目已逻辑删除", true) : new Result<>(0, "题目不存在或已被删除", false);
    }

    @RequestMapping(value = "/delQuestion", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Boolean> legacyDelete(@RequestParam Integer id) { return delete(id); }
}
