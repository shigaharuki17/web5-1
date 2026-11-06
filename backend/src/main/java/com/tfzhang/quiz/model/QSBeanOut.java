package com.tfzhang.quiz.model;

import java.util.List;

public record QSBeanOut(String question, List<AnsBean> answers) { }
