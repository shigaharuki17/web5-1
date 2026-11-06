package com.tfzhang.quiz.mapper;

import com.tfzhang.quiz.model.Question;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionMapper {
    String COLUMNS = "id,questionText,answer1Text,answer1Correct,answer2Text,answer2Correct,answer3Text,answer3Correct,answer4Text,answer4Correct,isDelete,createTime,updateTime";
    String FILTER = " FROM questions WHERE isDelete=0 AND questionText LIKE CONCAT('%',#{keyword},'%') ESCAPE '!'";

    @Insert("INSERT INTO questions (questionText,answer1Text,answer1Correct,answer2Text,answer2Correct,answer3Text,answer3Correct,answer4Text,answer4Correct,isDelete,createTime,updateTime) VALUES (#{questionText},#{answer1Text},#{answer1Correct},#{answer2Text},#{answer2Correct},#{answer3Text},#{answer3Correct},#{answer4Text},#{answer4Correct},#{isDelete},#{createTime},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuestion(Question question);

    @Select("SELECT " + COLUMNS + " FROM questions WHERE id=#{id} AND isDelete=0")
    Question findById(@Param("id") Integer id);

    @Update("UPDATE questions SET questionText=#{questionText},answer1Text=#{answer1Text},answer1Correct=#{answer1Correct},answer2Text=#{answer2Text},answer2Correct=#{answer2Correct},answer3Text=#{answer3Text},answer3Correct=#{answer3Correct},answer4Text=#{answer4Text},answer4Correct=#{answer4Correct},updateTime=#{updateTime} WHERE id=#{id} AND isDelete=0")
    int updateQuestion(Question question);

    @Update("UPDATE questions SET isDelete=1,updateTime=NOW() WHERE id=#{id} AND isDelete=0")
    int delQuestionById(@Param("id") Integer id);

    @Select("SELECT COUNT(*)" + FILTER)
    long count(@Param("keyword") String keyword);

    @Select("SELECT " + COLUMNS + FILTER + " ORDER BY id DESC LIMIT #{start},#{pageSize}")
    List<Question> page(@Param("start") int start, @Param("pageSize") int pageSize, @Param("keyword") String keyword);

    @Select("SELECT " + COLUMNS + FILTER + " ORDER BY id DESC")
    List<Question> findByName(@Param("keyword") String keyword);

    @Select("SELECT " + COLUMNS + " FROM questions WHERE isDelete=0 ORDER BY RAND() LIMIT 5")
    List<Question> getQuestions();
}
