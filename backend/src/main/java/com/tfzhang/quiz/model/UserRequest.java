package com.tfzhang.quiz.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"password", "checkpassword"})
public class UserRequest {
    @JsonAlias("userName")
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonAlias({"confirmPassword", "confirmpassword"})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String checkpassword;
}
