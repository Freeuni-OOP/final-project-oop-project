package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerCreatesUserWithHashedPasswordAndRedirectsHome() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "mvc_register_user")
                        .param("password", "secret")
                        .param("confirm", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        User saved = userRepository.findByUsername("mvc_register_user").orElseThrow();
        assertNotEquals("secret", saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("$2"));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "mvc_login_user")
                        .param("password", "secret")
                        .param("confirm", "secret"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/login")
                        .param("username", "mvc_login_user")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }
}
