package com.tfzhang.quiz.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SimpleUser {
    @NotBlank(message = "姓名不能为空")
    private String name;
    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于 0")
    @Max(value = 150, message = "年龄不能大于 150")
    private Integer age;
}
