package com.tfzhang.quiz.service.impl;

import com.tfzhang.quiz.mapper.UserMapper;
import com.tfzhang.quiz.model.User;
import com.tfzhang.quiz.model.UserRequest;
import com.tfzhang.quiz.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserMapper mapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserServiceImpl(UserMapper mapper) { this.mapper = mapper; }

    @Override
    @Transactional(readOnly = true)
    public List<User> list(String keyword) {
        if (keyword == null || keyword.isBlank()) return mapper.list();
        if (keyword.length() > 100) throw bad("查询关键字过长");
        return mapper.search(keyword.trim());
    }

    @Override
    public User addUser(String username, String password, String checkpassword) {
        validateUsername(username);
        validatePassword(password, checkpassword);
        if (mapper.existsByName(username) > 0) throw conflict();
        User user = new User();
        user.setUserName(username);
        user.setUserPassword(encoder.encode(password));
        user.setUserRole(0);
        user.setIsDelete(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(user.getCreateTime());
        if (mapper.saveUser(user) != 1 || user.getId() == null) throw new IllegalStateException("新增用户失败");
        return user;
    }

    @Override
    public User updateUser(Long id, UserRequest request) {
        validateId(id);
        validateUsername(request.getUsername());
        User existing = mapper.findById(id);
        if (existing == null) throw missing();
        User duplicate = mapper.findByUsername(request.getUsername());
        if (duplicate != null && !duplicate.getId().equals(id)) throw conflict();
        if (!isEmpty(request.getPassword()) || !isEmpty(request.getCheckpassword())) {
            validatePassword(request.getPassword(), request.getCheckpassword());
            existing.setUserPassword(encoder.encode(request.getPassword()));
        }
        existing.setUserName(request.getUsername());
        existing.setUpdateTime(LocalDateTime.now());
        if (mapper.updateUser(existing) != 1) throw missing();
        return existing;
    }

    @Override public boolean deleteUserById(Long id) { validateId(id); return mapper.deleteUserById(id) > 0; }
    @Override public boolean deleteUserByName(String username) { validateUsername(username); return mapper.deleteUserByUsername(username) > 0; }
    @Override public boolean hardDeleteById(Long id) { validateId(id); return mapper.hardDeleteById(id) > 0; }
    @Override public boolean hardDeleteByName(String username) { validateUsername(username); return mapper.hardDeleteByUsername(username) > 0; }

    private void validateUsername(String value) {
        if (value == null || value.isBlank()) throw bad("用户名不能为空");
        if (value.length() < 4 || value.length() > 20) throw bad("用户名长度为 4–20 个字符");
        if (!value.matches("^[a-zA-Z0-9]+$")) throw bad("用户名只能包含英文字母和数字");
    }

    private void validatePassword(String password, String confirmation) {
        if (password == null || password.isBlank() || confirmation == null || confirmation.isBlank()) throw bad("密码和确认密码不能为空");
        if (password.length() < 8 || password.length() > 64 || password.getBytes(StandardCharsets.UTF_8).length > 72) throw bad("密码需为 8–64 位且 UTF-8 编码不超过 72 字节");
        if (!password.equals(confirmation)) throw bad("两次输入的密码不一致");
    }

    private boolean isEmpty(String value) { return value == null || value.isEmpty(); }
    private void validateId(Long id) { if (id == null || id <= 0) throw bad("用户 ID 必须为正整数"); }
    private ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException conflict() { return new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在"); }
    private ResponseStatusException missing() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在或已被删除"); }
}
