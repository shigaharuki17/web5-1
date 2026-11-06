package com.tfzhang.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfzhang.quiz.mapper.*;
import com.tfzhang.quiz.model.*;
import com.tfzhang.quiz.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaginationAndQuestionTests {
    @Autowired UserService users;
    @Autowired QuestionService questions;
    @Autowired QuestionMapper mapper;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @Test void usersArePaginatedInSqlAndExcludeDeleted() throws Exception {
        List<User> data=new ArrayList<>();
        for(int i=0;i<7;i++) data.add(users.addUser("pageUser"+i,"Secret123","Secret123"));
        users.deleteUserById(data.get(0).getId());
        PageBean<User> first=users.page(1,5,""); PageBean<User> second=users.page(2,5,"");
        assertThat(first.total()).isEqualTo(6); assertThat(first.rows()).hasSize(5); assertThat(second.rows()).hasSize(1);
        assertThat(first.rows()).extracting(User::getId).containsExactly(data.get(6).getId(),data.get(5).getId(),data.get(4).getId(),data.get(3).getId(),data.get(2).getId());
        String body=mvc.perform(get("/users").param("page","2").param("pageSize","5")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(6)).andExpect(jsonPath("$.data.rows.length()").value(1)).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("userPassword","Secret123","$2a$");
    }

    @Test void userKeywordAndEmptyPaginationWork() throws Exception {
        users.addUser("alphaUser","Secret123","Secret123");users.addUser("betaUser","Secret123","Secret123");
        mvc.perform(get("/findUser").param("keyword","ALPHA")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(get("/api/users/page").param("keyword","beta")).andExpect(jsonPath("$.data.total").value(1)).andExpect(jsonPath("$.data.rows[0].userName").value("betaUser"));
        assertThat(users.page(99,5,"").rows()).isEmpty(); assertThat(users.page(1,5,"missing").total()).isZero();
        assertThat(users.page(1,5,"%").rows()).isEmpty();
    }

    @ParameterizedTest @CsvSource({"0,5","-1,5","1,0","1,101","2147483647,100"})
    void invalidPaginationRejectedForBothEntities(int page,int size) throws Exception {
        for(String path:List.of("/users","/questions")) mvc.perform(get(path).param("page",String.valueOf(page)).param("pageSize",String.valueOf(size)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }

    @Test void questionInsertMapsOptionsAndGeneratedId() {
        QSBeanOutManage result=questions.insertQuestion(input("法国的首都是哪里？","B"));
        assertThat(result.id()).isPositive(); assertThat(result.answer()).isEqualTo("选项B"); assertThat(result.answerKey()).isEqualTo("b");
        Question row=mapper.findById(result.id()); assertThat(row.getAnswer2Correct()).isTrue();
        assertThat(row.getAnswer1Correct()).isFalse();assertThat(row.getAnswer3Correct()).isFalse();assertThat(row.getAnswer4Correct()).isFalse();
        assertThat(row.getIsDelete()).isZero();assertThat(row.getCreateTime()).isNotNull();
    }

    @Test void jsonAndFormQuestionEndpointsWork() throws Exception {
        mvc.perform(post("/addQuestion").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input("JSON 题目","a"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").isNumber()).andExpect(jsonPath("$.data.options.length()").value(4));
        mvc.perform(post("/addQuestion").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("question","程序语言题").param("optiona","Java").param("optionb","Apple").param("optionc","Python").param("optiond","Javascript").param("answer","b"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.answer").value("Apple"));
    }

    @ParameterizedTest @CsvSource({"a,0","B,1","c,2","D,3"})
    void exactlyOneAnswerMapsForEveryLetter(String answer,int index) {
        var q=questions.insertQuestion(input("映射题 "+answer,answer));
        assertThat(q.answerKey()).isEqualTo(answer.toLowerCase(Locale.ROOT));
        var random=questions.getQuestions().get(0);
        assertThat(random.answers()).filteredOn(AnsBean::correct).hasSize(1);
        assertThat(random.answers().get(index).correct()).isTrue();
    }

    @Test void missingBlankDuplicateAndInvalidAnswerRejected() throws Exception {
        for(String kind:List.of("blank","missing","duplicate","invalidAnswer","nullAnswer","tooLong")) {
            QSBean value=input("校验题","a");
            switch(kind) {
                case "blank" -> value.setQuestion("   ");
                case "missing" -> value.setOptiond(null);
                case "duplicate" -> {value.setOptiona("same");value.setOptionb(" SAME ");}
                case "invalidAnswer" -> value.setAnswer("e");
                case "nullAnswer" -> value.setAnswer(null);
                case "tooLong" -> value.setQuestion("长".repeat(256));
            }
            mvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(value)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        }
        assertThat(questions.page(1,5,"").total()).isZero();
    }

    @Test void managementPaginationAndKeywordUseSameActiveCount() throws Exception {
        List<Integer> ids=new ArrayList<>();for(int i=0;i<7;i++)ids.add(questions.insertQuestion(input("分页测试 "+i,"a")).id());
        questions.delQuestion(ids.get(0));
        QSBeanPage first=questions.page(1,5,"分页");QSBeanPage second=questions.page(2,5,"分页");
        assertThat(first.total()).isEqualTo(6);assertThat(first.qsBeanList()).hasSize(5);assertThat(second.qsBeanList()).hasSize(1);
        assertThat(first.qsBeanList().get(0).id()).isEqualTo(ids.get(6));
        mvc.perform(get("/questions").param("page","2").param("pageSize","5")).andExpect(jsonPath("$.data.total").value(6)).andExpect(jsonPath("$.data.qsBeanList.length()").value(1));
        mvc.perform(get("/findQuestion").param("keyword","测试 6")).andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test void literalWildcardsAndSqlInjectionAreNotSpecial() {
        questions.insertQuestion(input("100%_题目!","a"));questions.insertQuestion(input("1000普通题目","a"));
        assertThat(questions.findByName("%")).hasSize(1);assertThat(questions.findByName("_")).hasSize(1);assertThat(questions.findByName("!")).hasSize(1);
        assertThat(questions.findByName("' OR 1=1 --")).isEmpty();
    }

    @Test void randomOutputContainsAtMostFiveDistinctActiveQuestions() throws Exception {
        List<Integer> ids=new ArrayList<>();for(int i=0;i<8;i++)ids.add(questions.insertQuestion(input("随机题 "+i,"c")).id());
        questions.delQuestion(ids.get(0));
        var random=questions.getQuestions();assertThat(random).hasSize(5);assertThat(random).extracting(QSBeanOut::question).doesNotHaveDuplicates().doesNotContain("随机题 0");
        assertThat(random).allSatisfy(q->{assertThat(q.answers()).hasSize(4);assertThat(q.answers()).filteredOn(AnsBean::correct).hasSize(1);});
        mvc.perform(get("/getQuestion")).andExpect(jsonPath("$.data.length()").value(5)).andExpect(jsonPath("$.data[0].answers.length()").value(4)).andExpect(jsonPath("$.data[0].isDelete").doesNotExist());
    }

    @Test void randomOutputHandlesEmptyAndSmallQuestionBanks() {
        assertThat(questions.getQuestions()).isEmpty();questions.insertQuestion(input("唯一题目","a"));assertThat(questions.getQuestions()).hasSize(1);
    }

    @Test void logicalDeleteRetainsRowAndFiltersAllOutputs() throws Exception {
        var q=questions.insertQuestion(input("需要删除的题目","a"));
        mvc.perform(get("/delQuestion").param("id",q.id().toString())).andExpect(jsonPath("$.code").value(1));
        assertThat(jdbc.queryForObject("SELECT isDelete FROM questions WHERE id=?",Integer.class,q.id())).isEqualTo(1);
        assertThat(mapper.findById(q.id())).isNull();assertThat(questions.page(1,5,"").total()).isZero();assertThat(questions.findByName("题目")).isEmpty();assertThat(questions.getQuestions()).isEmpty();
        mvc.perform(delete("/api/questions/"+q.id())).andExpect(jsonPath("$.code").value(0));
    }

    @Test void editingUpdatesAllFieldsAndPreservesCreationTime() throws Exception {
        var q=questions.insertQuestion(input("原始题目","a"));var time=mapper.findById(q.id()).getCreateTime();
        mvc.perform(put("/api/questions/"+q.id()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input("修改题目","D"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.answerKey").value("d"));
        assertThat(mapper.findById(q.id()).getCreateTime()).isEqualTo(time);questions.delQuestion(q.id());
        mvc.perform(put("/api/questions/"+q.id()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input("不能修改","a")))).andExpect(status().isNotFound());
    }

    @Test void emptyResultsAndBadIdsHaveUniformResponses() throws Exception {
        mvc.perform(get("/questions")).andExpect(jsonPath("$.data.total").value(0)).andExpect(jsonPath("$.data.qsBeanList").isArray());
        mvc.perform(get("/getQuestion")).andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/delQuestion").param("id","bad")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(delete("/api/questions/-1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(get("/questions").param("keyword","x".repeat(256))).andExpect(status().isBadRequest());
    }

    private QSBean input(String title,String answer) {QSBean q=new QSBean();q.setQuestion(title);q.setOptiona("选项A");q.setOptionb("选项B");q.setOptionc("选项C");q.setOptiond("选项D");q.setAnswer(answer);return q;}
}
