package com.tfzhang.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@ToString(exclude = "userPassword")
public class User {
    private Long id;
    private String userName;
    @JsonIgnore
    private String userPassword;
    private Integer isDelete;
    private Integer userRole;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
