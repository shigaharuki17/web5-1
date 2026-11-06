package com.tfzhang.quiz.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Question {
    private Integer id;
    private String questionText;
    private String answer1Text;
    private Boolean answer1Correct;
    private String answer2Text;
    private Boolean answer2Correct;
    private String answer3Text;
    private Boolean answer3Correct;
    private String answer4Text;
    private Boolean answer4Correct;
    private Integer isDelete;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
