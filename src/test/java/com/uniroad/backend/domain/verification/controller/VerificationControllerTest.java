package com.uniroad.backend.domain.verification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.dto.RejectRequest;
import com.uniroad.backend.domain.verification.dto.VerificationResponse;
import com.uniroad.backend.domain.verification.entity.VerificationStatus;
import com.uniroad.backend.domain.verification.service.VerificationService;
import com.uniroad.backend.global.config.SecurityConfig;
import com.uniroad.backend.global.jwt.JwtExceptionFilter;
import com.uniroad.backend.global.jwt.JwtProvider;
import com.uniroad.backend.global.oauth2.CustomOAuth2UserService;
import com.uniroad.backend.global.oauth2.OAuth2FailureHandler;
import com.uniroad.backend.global.oauth2.OAuth2SuccessHandler;
import com.uniroad.backend.global.security.CustomUserDetails;
import com.uniroad.backend.global.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VerificationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VerificationService verificationService;

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
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtExceptionFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("submitVerification returns verification response")
    void submitVerification_Success() throws Exception {
        // given
        VerificationResponse response = new VerificationResponse(
                1L,
                "https://example.com/verification.png",
                VerificationStatus.PENDING,
                null,
                LocalDateTime.now(),
                null
        );
        given(verificationService.submitVerification(eq(1L), eq("https://example.com/verification.png")))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/verifications")
                        .with(user(userDetails(1L, Role.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrl\":\"https://example.com/verification.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/verification.png"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("getPendingVerifications returns pending list for admin")
    void getPendingVerifications_Success() throws Exception {
        // given
        VerificationResponse verification = new VerificationResponse(
                1L,
                "image",
                VerificationStatus.PENDING,
                null,
                LocalDateTime.now(),
                null
        );
        AdminVerificationResponse response = new AdminVerificationResponse(
                1L,
                "Admin Target",
                "target@test.com",
                verification
        );
        given(verificationService.getPendingVerifications()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/verifications/pending")
                        .with(user(userDetails(99L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].memberId").value(1))
                .andExpect(jsonPath("$.data[0].memberEmail").value("target@test.com"))
                .andExpect(jsonPath("$.data[0].verification.status").value("PENDING"));
    }

    @Test
    @DisplayName("getMyVerifications returns my verification history")
    void getMyVerifications_Success() throws Exception {
        // given
        VerificationResponse response = new VerificationResponse(
                1L,
                "my-image",
                VerificationStatus.REJECTED,
                "invalid image",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(verificationService.getMyVerifications(1L)).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/verifications/me")
                        .with(user(userDetails(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].imageUrl").value("my-image"))
                .andExpect(jsonPath("$.data[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.data[0].rejectReason").value("invalid image"));

        verify(verificationService).getMyVerifications(1L);
    }

    @Test
    @DisplayName("approveVerification calls service")
    void approveVerification_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/verifications/1/approve")
                        .with(user(userDetails(99L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(verificationService).approveVerification(1L);
    }

    @Test
    @DisplayName("rejectVerification calls service with reason")
    void rejectVerification_Success() throws Exception {
        // given
        RejectRequest request = new RejectRequest("invalid image");

        // when & then
        mockMvc.perform(post("/api/v1/verifications/1/reject")
                        .with(user(userDetails(99L, Role.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(verificationService).rejectVerification(1L, "invalid image");
    }

    private CustomUserDetails userDetails(Long memberId, Role role) {
        Member member = Member.builder()
                .id(memberId)
                .email("user" + memberId + "@test.com")
                .password("password")
                .name("User " + memberId)
                .role(role)
                .provider("LOCAL")
                .build();
        return new CustomUserDetails(member);
    }
}
