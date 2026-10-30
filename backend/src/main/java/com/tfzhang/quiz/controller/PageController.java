package com.tfzhang.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/user", "/question", "/question/add"})
    public String page() { return "forward:/index.html"; }
}
