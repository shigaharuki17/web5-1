package com.tfzhang.quiz.model;

import java.util.List;

public record PageBean<T>(long total, List<T> rows) { }
