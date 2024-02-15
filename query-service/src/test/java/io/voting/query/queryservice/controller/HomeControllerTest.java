package io.voting.query.queryservice.controller;

import io.voting.query.queryservice.config.SecurityConfig;
import io.voting.query.queryservice.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({HomeController.class, AuthController.class})
@Import({SecurityConfig.class, TokenService.class})
class HomeControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void rootWhenUnAuthenticatedThen401() throws Exception {
    mvc.perform(get("/"))
            .andExpect(status().isUnauthorized());
  }

  @Test
  void rootWhenAuthenticatedThenSaysHelloUser() throws Exception {
    final MvcResult result = mvc.perform(post("/token")
                    .with(httpBasic("test", "password")))
            .andExpect(status().isOk())
            .andReturn();
    final String token = result.getResponse().getContentAsString();

    mvc.perform(get("/")
            .header("Authorization", "Bearer " + token))
            .andExpect(content().string("Hello, test"));
  }

  @Test
  @WithMockUser
  void rootWithMockUserStatusIsOK() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
  }

}