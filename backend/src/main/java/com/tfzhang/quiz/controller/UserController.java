package com.tfzhang.quiz.controller;

import com.tfzhang.quiz.model.*;
import com.tfzhang.quiz.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @GetMapping({"/api/users", "/users"})
    public Result<List<User>> list(@RequestParam(required = false) String username) {
        return Result.success(service.list(username));
    }

    @PostMapping(value = {"/api/users", "/register"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Result<User> register(@RequestBody UserRequest request) {
        return Result.success("新增用户成功", service.addUser(request.getUsername(), request.getPassword(), request.getCheckpassword()));
    }

    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public Result<User> registerForm(@ModelAttribute UserRequest request) {
        return register(request);
    }

    @PutMapping("/api/users/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody UserRequest request) {
        return Result.success("用户信息已更新", service.updateUser(id, request));
    }

    @DeleteMapping("/api/users/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) { return deleted(service.deleteUserById(id), false); }

    @DeleteMapping("/api/users/by-name/{username}")
    public Result<Boolean> deleteByName(@PathVariable String username) { return deleted(service.deleteUserByName(username), false); }

    @DeleteMapping("/api/users/{id}/hard")
    public Result<Boolean> hardDeleteById(@PathVariable Long id) { return deleted(service.hardDeleteById(id), true); }

    @DeleteMapping("/api/users/by-name/{username}/hard")
    public Result<Boolean> hardDeleteByName(@PathVariable String username) { return deleted(service.hardDeleteByName(username), true); }

    // 与课程 Apifox 示例保持兼容；页面使用上方 DELETE 接口。
    @RequestMapping(value = "/deleteById", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Boolean> legacyDeleteId(@RequestParam Long id) { return deleteById(id); }

    @RequestMapping(value = "/deleteByName", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Boolean> legacyDeleteName(@RequestParam String username) { return deleteByName(username); }

    @PostMapping("/hardDeleteById")
    public Result<Boolean> legacyHardDeleteId(@RequestParam Long id) { return hardDeleteById(id); }

    @PostMapping("/hardDeleteByName")
    public Result<Boolean> legacyHardDeleteName(@RequestParam String username) { return hardDeleteByName(username); }

    private Result<Boolean> deleted(boolean success, boolean hard) {
        return success ? Result.success(hard ? "用户记录已物理删除" : "用户已逻辑删除", true)
                : new Result<>(0, "用户不存在或已被删除", false);
    }
}
