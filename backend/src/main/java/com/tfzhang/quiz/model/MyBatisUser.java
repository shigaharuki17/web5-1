package com.tfzhang.quiz.model;

import lombok.Data;

@Data
public class MyBatisUser {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
