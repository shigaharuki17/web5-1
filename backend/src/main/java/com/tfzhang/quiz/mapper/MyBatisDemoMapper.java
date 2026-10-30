package com.tfzhang.quiz.mapper;

import com.tfzhang.quiz.model.MyBatisUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MyBatisDemoMapper {
    @Select("SELECT id,name,age,email FROM mybatis_test_user ORDER BY id")
    List<MyBatisUser> list();
}
