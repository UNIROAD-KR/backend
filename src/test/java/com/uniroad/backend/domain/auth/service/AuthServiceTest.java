package com.uniroad.backend.domain.auth.service;

import com.uniroad.backend.domain.auth.dto.LoginRequest;
import com.uniroad.backend.domain.auth.dto.OnboardingRequest;
import com.uniroad.backend.domain.auth.dto.SignUpRequest;
import com.uniroad.backend.domain.auth.dto.TokenResponse;
import com.uniroad.backend.domain.auth.entity.RefreshToken;
import com.uniroad.backend.domain.auth.repository.RefreshTokenRepository;
import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.domain.member.entity.CurrentSituation;
import com.uniroad.backend.domain.member.entity.Gender;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.member.repository.MemberSocialAccountRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private SocialAuthService socialAuthService;

    @Mock
    private UniversityRepository universityRepository;

    @Mock
    private MemberSocialAccountRepository memberSocialAccountRepository;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        SignUpRequest request = new SignUpRequest("test1234", "test@test.com", "Password123!", "테스터");
        given(memberRepository.findByUsername(request.username())).willReturn(Optional.empty());
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
    @DisplayName("회원가입 실패 - 중복된 아이디")
    void signUp_Fail_DuplicateUsername() {
        // given
        SignUpRequest request = new SignUpRequest("test1234", "test@test.com", "Password123!", "테스터");
        given(memberRepository.findByUsername(request.username())).willReturn(Optional.of(Member.builder().build()));

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);
    }

    @Test
    @DisplayName("온보딩 성공 - 추가 회원 정보 저장")
    void onboarding_Success_SaveAdditionalProfile() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스터")
                .role(Role.USER)
                .build();
        University university = University.builder()
                .id(1L)
                .name("한국대학교")
                .build();
        OnboardingRequest request = new OnboardingRequest(
                23,
                "유니",
                Gender.FEMALE,
                CurrentSituation.PREPARING_APPLICATION,
                "한국대학교",
                "도쿄대학교",
                "일본",
                "도쿄"
        );

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(universityRepository.findByName("한국대학교")).willReturn(Optional.of(university));

        // when
        authService.onboarding(1L, request);

        // then
        assertThat(member.getAge()).isEqualTo(23);
        assertThat(member.getNickname()).isEqualTo("유니");
        assertThat(member.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(member.getCurrentSituation()).isEqualTo(CurrentSituation.PREPARING_APPLICATION);
        assertThat(member.getDomesticUniversity()).isEqualTo(university);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest("test1234", "Password123!");
        Member member = Member.builder()
                .id(1L)
                .username("test1234")
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .status(com.uniroad.backend.domain.member.entity.MemberStatus.ACTIVE)
                .build();

        given(memberRepository.findByUsername(request.username())).willReturn(Optional.of(member));
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
        LoginRequest request = new LoginRequest("test1234", "WrongPassword");
        Member member = Member.builder()
                .username("test1234")
                .email("test@test.com")
                .password("encodedPassword")
                .build();

        given(memberRepository.findByUsername(request.username())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

}
