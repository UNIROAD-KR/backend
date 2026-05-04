package com.uniroad.backend.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniroad.backend.domain.auth.dto.LoginRequest;
import com.uniroad.backend.domain.auth.dto.SignUpRequest;
import com.uniroad.backend.domain.auth.dto.TokenResponse;
import com.uniroad.backend.domain.auth.service.AuthService;
import com.uniroad.backend.global.config.SecurityConfig;
import com.uniroad.backend.global.jwt.JwtExceptionFilter;
import com.uniroad.backend.global.jwt.JwtProvider;
import com.uniroad.backend.global.oauth2.CustomOAuth2UserService;
import com.uniroad.backend.global.oauth2.OAuth2FailureHandler;
import com.uniroad.backend.global.oauth2.OAuth2SuccessHandler;
import com.uniroad.backend.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
@org.springframework.test.context.ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // SecurityConfig에 필요한 Bean들 Mocking
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;
    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;
    @MockBean
    private JwtExceptionFilter jwtExceptionFilter;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        // JwtExceptionFilter가 모킹되어 있으므로, 필터 체인을 계속 진행하도록 설정해야 컨트롤러에 도달함
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtExceptionFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("회원가입 요청 성공")
    @WithMockUser // Spring Security 필터 통과를 위해 Mock 사용자 설정
    void signUp_Success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("test@test.com", "Password123!", "테스터");
        given(authService.signUp(any(SignUpRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/auth/sign-up")
                        .with(csrf()) // CSRF 필터 대응
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("로그인 요청 성공")
    @WithMockUser
    void login_Success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "Password123!");
        TokenResponse response = TokenResponse.of("access", "refresh", 1800L);
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.accessToken").value("access"));
    }
}
