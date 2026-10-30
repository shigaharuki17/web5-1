package com.tfzhang.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfzhang.quiz.mapper.*;
import com.tfzhang.quiz.model.User;
import com.tfzhang.quiz.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizApplicationTests {
    @Autowired UserMapper userMapper;
    @Autowired MyBatisDemoMapper demoMapper;
    @Autowired UserService service;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @Test void testListMyBatisFiveUsers() {
        assertThat(demoMapper.list()).hasSize(5).extracting("name").containsExactly("Jone", "Jack", "Tom", "Sandy", "Billie");
    }

    @Test void testSaveUserGeneratedId() {
        User user = new User(); user.setUserName("mapperTest");
        user.setUserPassword(new BCryptPasswordEncoder().encode("Password123"));
        user.setUserRole(0); user.setIsDelete(0); user.setCreateTime(LocalDateTime.now()); user.setUpdateTime(user.getCreateTime());
        assertThat(userMapper.saveUser(user)).isEqualTo(1);
        assertThat(user.getId()).isPositive();
        assertThat(userMapper.existsByName("mapperTest")).isEqualTo(1);
        assertThat(userMapper.findByUsername("mapperTest").getId()).isEqualTo(user.getId());
    }

    @Test void helloHasUnifiedResult() throws Exception {
        mvc.perform(get("/hello")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data").value("Hello World"));
    }

    @Test void getAndPostParameters() throws Exception {
        mvc.perform(get("/simpleParam").param("name", "tom").param("age", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.data.age").value(10));
        mvc.perform(post("/simpleParam").param("name", "tom").param("age", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        mvc.perform(post("/simpleUser").param("name", "tom").param("age", "10")).andExpect(status().isOk()).andExpect(jsonPath("$.data.name").value("tom"));
    }

    @Test void invalidSimpleParametersAreRejected() throws Exception {
        mvc.perform(get("/simpleUser").param("name", "tom").param("age", "-1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(get("/simpleParam").param("name", "tom").param("age", "invalid")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }

    @Test void registrationHashesPasswordAndNeverReturnsIt() throws Exception {
        String response = mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body("alice123", "Secret123", "Secret123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1)).andExpect(jsonPath("$.data.id").isNumber())
            .andExpect(jsonPath("$.data.userPassword").doesNotExist()).andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("Secret123", "$2a$", "checkpassword");
        User user = userMapper.findByUsername("alice123");
        assertThat(new BCryptPasswordEncoder().matches("Secret123", user.getUserPassword())).isTrue();
        assertThat(user.getUserRole()).isZero(); assertThat(user.getIsDelete()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"abc,Secret123,Secret123", "bad_name,Secret123,Secret123", "bad'name,Secret123,Secret123", "validuser,1234567,1234567", "validuser,Secret123,Other123"})
    void invalidRegistrationIsRejected(String name, String password, String confirmation) throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body(name,password,confirmation)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        assertThat(userMapper.existsByName(name)).isZero();
    }

    @Test void missingConfirmationIsRejected() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"alice123\",\"password\":\"Secret123\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }

    @Test void duplicateUsernameIsRejectedCaseInsensitively() throws Exception {
        service.addUser("alice123", "Secret123", "Secret123");
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body("ALICE123","Secret123","Secret123")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.msg").value("用户名已存在"));
    }

    @Test void databaseUniqueKeyProtectsAgainstDuplicateInserts() {
        User original = service.addUser("alice123", "Secret123", "Secret123");
        original.setId(null);
        assertThatThrownBy(() -> userMapper.saveUser(original)).isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
    }

    @Test void formRegistrationWorks() throws Exception {
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("username", "formuser").param("password", "Secret123").param("checkpassword", "Secret123"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.userName").value("formuser"));
    }

    @Test void listFiltersAndNeverReturnsHashes() throws Exception {
        service.addUser("alice123", "Secret123", "Secret123"); service.addUser("bobby123", "Secret123", "Secret123");
        String body = mvc.perform(get("/api/users").param("username", "alice")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1))
            .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("userPassword", "Secret123", "$2a$");
        mvc.perform(get("/api/users").param("username", "' OR 1=1 --")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test void editCanPreserveOrResetPassword() throws Exception {
        User user=service.addUser("alice123","Secret123","Secret123"); String original=user.getUserPassword();
        mvc.perform(put("/api/users/"+user.getId()).contentType(MediaType.APPLICATION_JSON).content(body("alice456","","")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.userPassword").doesNotExist());
        assertThat(userMapper.findById(user.getId()).getUserPassword()).isEqualTo(original);
        mvc.perform(put("/api/users/"+user.getId()).contentType(MediaType.APPLICATION_JSON).content(body("alice456","Changed123","Other123"))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/"+user.getId()).contentType(MediaType.APPLICATION_JSON).content(body("alice456","Changed123","Changed123"))).andExpect(status().isOk());
        assertThat(new BCryptPasswordEncoder().matches("Changed123",userMapper.findById(user.getId()).getUserPassword())).isTrue();
    }

    @Test void softDeleteByIdRetainsDatabaseRecord() throws Exception {
        User user=service.addUser("alice123","Secret123","Secret123");
        mvc.perform(delete("/api/users/"+user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1));
        assertThat(userMapper.findById(user.getId())).isNull();
        assertThat(jdbc.queryForObject("SELECT isDelete FROM `user` WHERE id=?",Integer.class,user.getId())).isEqualTo(1);
        assertThat(service.list(null)).isEmpty();
        mvc.perform(get("/deleteById").param("id",user.getId().toString())).andExpect(jsonPath("$.code").value(0));
    }

    @Test void softDeleteByNameAndUsernameReuseWork() throws Exception {
        User first=service.addUser("alice123","Secret123","Secret123");
        mvc.perform(get("/deleteByName").param("username","alice123")).andExpect(jsonPath("$.code").value(1));
        User second=service.addUser("alice123","Secret123","Secret123");
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(service.deleteUserByName("alice123")).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM `user` WHERE userName='alice123' AND isDelete=1",Integer.class)).isEqualTo(2);
    }

    @Test void hardDeleteByIdActuallyRemovesRecord() throws Exception {
        User user=service.addUser("alice123","Secret123","Secret123");
        mvc.perform(delete("/api/users/"+user.getId()+"/hard")).andExpect(jsonPath("$.code").value(1));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM `user` WHERE id=?",Integer.class,user.getId())).isZero();
    }

    @Test void hardDeleteByNameAlsoRemovesDeletedRecords() throws Exception {
        service.addUser("alice123","Secret123","Secret123"); service.deleteUserByName("alice123");
        mvc.perform(post("/hardDeleteByName").param("username","alice123")).andExpect(jsonPath("$.code").value(1));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM `user` WHERE userName='alice123'",Integer.class)).isZero();
    }

    @Test void invalidIdAndMalformedJsonReturnUnifiedErrors() throws Exception {
        mvc.perform(delete("/api/users/-1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(get("/deleteById").param("id","bad")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        mvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content("{")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
    }

    private String body(String name,String password,String confirmation) throws Exception {
        return json.writeValueAsString(Map.of("username",name,"password",password,"checkpassword",confirmation));
    }
}
