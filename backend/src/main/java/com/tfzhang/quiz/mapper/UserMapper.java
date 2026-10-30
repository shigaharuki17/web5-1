package com.tfzhang.quiz.mapper;

import com.tfzhang.quiz.model.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    String COLUMNS = "id, userName, userPassword, isDelete, userRole, createTime, updateTime";

    @Select("SELECT " + COLUMNS + " FROM `user` WHERE isDelete = 0 ORDER BY id DESC")
    List<User> list();

    @Select("SELECT " + COLUMNS + " FROM `user` WHERE isDelete = 0 AND LOCATE(#{keyword}, userName) > 0 ORDER BY id DESC")
    List<User> search(@Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM `user` WHERE activeName = LOWER(#{username})")
    int existsByName(@Param("username") String username);

    @Select("SELECT " + COLUMNS + " FROM `user` WHERE id = #{id} AND isDelete = 0")
    User findById(@Param("id") Long id);

    @Select("SELECT " + COLUMNS + " FROM `user` WHERE activeName = LOWER(#{username})")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO `user` (userName,userPassword,isDelete,userRole,createTime,updateTime) VALUES (#{userName},#{userPassword},#{isDelete},#{userRole},#{createTime},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveUser(User user);

    @Update("UPDATE `user` SET userName=#{userName},userPassword=#{userPassword},updateTime=#{updateTime} WHERE id=#{id} AND isDelete=0")
    int updateUser(User user);

    @Update("UPDATE `user` SET isDelete=1,updateTime=NOW() WHERE id=#{id} AND isDelete=0")
    int deleteUserById(@Param("id") Long id);

    @Update("UPDATE `user` SET isDelete=1,updateTime=NOW() WHERE activeName=LOWER(#{username})")
    int deleteUserByUsername(@Param("username") String username);

    @Delete("DELETE FROM `user` WHERE id=#{id}")
    int hardDeleteById(@Param("id") Long id);

    // 物理删除该名字对应的全部记录，包括历史逻辑删除记录。
    @Delete("DELETE FROM `user` WHERE LOWER(userName)=LOWER(#{username})")
    int hardDeleteByUsername(@Param("username") String username);
}
