package com.tfzhang.quiz.service;

import com.tfzhang.quiz.model.User;
import com.tfzhang.quiz.model.UserRequest;
import com.tfzhang.quiz.model.PageBean;
import java.util.List;

public interface UserService {
    List<User> list(String keyword);
    PageBean<User> page(Integer page, Integer pageSize, String keyword);
    User addUser(String username, String password, String checkpassword);
    User updateUser(Long id, UserRequest request);
    boolean deleteUserById(Long id);
    boolean deleteUserByName(String username);
    boolean hardDeleteById(Long id);
    boolean hardDeleteByName(String username);
}
