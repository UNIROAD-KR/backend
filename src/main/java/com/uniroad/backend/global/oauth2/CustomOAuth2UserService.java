package com.uniroad.backend.global.oauth2;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfo;
import com.uniroad.backend.global.oauth2.userinfo.OAuth2UserInfoFactory;
import com.uniroad.backend.global.security.CustomUserDetails;

import com.uniroad.backend.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 소셜 로그인 후 사용자 정보를 처리하는 서비스
 *
 * 흐름:
 * 1. 카카오/네이버 인증 서버에서 Access Token 획득 (Spring이 처리)
 * 2. 해당 토큰으로 사용자 정보 API 호출 (여기서 처리)
 * 3. DB에 회원 없으면 자동 가입 (소셜 로그인 최초 진입)
 * 4. 있으면 이름 동기화 후 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());

        Member member = saveOrUpdateSocialMember(registrationId, userInfo);

        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }

    private Member saveOrUpdateSocialMember(String provider, OAuth2UserInfo userInfo) {
        return memberRepository.findByProviderAndProviderId(provider, userInfo.getId())
                .map(existing -> {
                    existing.updateName(userInfo.getName());
                    return existing;
                })
                .orElseGet(() -> {
                    if (userInfo.getEmail() != null) {
                        return memberRepository.findByEmail(userInfo.getEmail())
                                .map(existing -> {
                                    existing.linkOAuth2(provider, userInfo.getId());
                                    return existing;
                                })
                                .orElseGet(() -> createSocialMember(provider, userInfo));
                    }
                    return createSocialMember(provider, userInfo);
                });
    }

    private Member createSocialMember(String provider, OAuth2UserInfo userInfo) {
        Member member = Member.builder()
                .email(userInfo.getEmail() != null
                        ? userInfo.getEmail()
                        : provider + "_" + userInfo.getId() + "@social.uniroad")
                .name(userInfo.getName() != null ? userInfo.getName() : "소셜회원")
                .provider(provider)
                .providerId(userInfo.getId())
                .role(Role.USER)
                .build();

        return memberRepository.save(member);
    }
}
