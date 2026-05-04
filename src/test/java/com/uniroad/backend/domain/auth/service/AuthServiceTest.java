package com.uniroad.backend.domain.auth.service;

import com.uniroad.backend.domain.auth.dto.LoginRequest;
import com.uniroad.backend.domain.auth.dto.SignUpRequest;
import com.uniroad.backend.domain.auth.dto.TokenResponse;
import com.uniroad.backend.domain.auth.entity.RefreshToken;
import com.uniroad.backend.domain.auth.repository.RefreshTokenRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest("test@test.com", "Password123!", "테스터");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
        
        Member savedMember = Member.builder()
                .id(1L)
                .email(request.email())
                .name(request.name())
                .build();
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // when
        Long memberId = authService.signUp(request);

        // then
        assertThat(memberId).isEqualTo(1L);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void signUp_Fail_DuplicateEmail() {
        // given
        SignUpRequest request = new SignUpRequest("test@test.com", "Password123!", "테스터");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "Password123!");
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);
        
        given(jwtProvider.createAccessToken(anyLong(), anyString())).willReturn("access-token");
        given(jwtProvider.createRefreshToken(anyLong())).willReturn("refresh-token");
        given(jwtProvider.getRefreshTokenValiditySeconds()).willReturn(3600L);
        given(jwtProvider.getAccessTokenValiditySeconds()).willReturn(1800L);
        given(refreshTokenRepository.findByMemberId(anyLong())).willReturn(Optional.empty());

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_InvalidPassword() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "WrongPassword");
        Member member = Member.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .build();

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    
}
