package com.tfzhang.quiz.controller;

import com.tfzhang.quiz.model.Result;
import com.tfzhang.quiz.model.SimpleUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public Result<String> hello() { return Result.success("Hello World"); }

    @RequestMapping(value = "/simpleParam", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<SimpleUser> simpleParam(@Valid @ModelAttribute SimpleUser user) {
        return Result.success(user);
    }

    @RequestMapping(value = "/simpleUser", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<SimpleUser> simpleUser(@Valid @ModelAttribute SimpleUser user) {
        return Result.success(user);
    }
}
