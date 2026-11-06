package com.tfzhang.quiz.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PageQuery {
    private PageQuery() { }
    public static int offset(Integer page, Integer pageSize) {
        if (page == null || page < 1 || pageSize == null || pageSize < 1 || pageSize > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "页码需大于 0，每页条数需为 1–100");
        long offset = (page - 1L) * pageSize;
        if (offset > Integer.MAX_VALUE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "页码过大");
        return (int) offset;
    }
    public static String keyword(String keyword) {
        if (keyword == null) return "";
        if (keyword.length() > 255) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "查询关键字过长");
        return keyword.trim();
    }
    public static String like(String keyword) {
        return keyword(keyword).replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
