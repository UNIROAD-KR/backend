package com.uniroad.backend.domain.member.service;

import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.domain.accountbook.repository.AccountBookRepository;
import com.uniroad.backend.domain.auth.repository.RefreshTokenRepository;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.dto.PasswordUpdateRequest;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberSocialAccount;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.member.repository.MemberSocialAccountRepository;
import com.uniroad.backend.domain.notification.repository.FcmTokenRepository;
import com.uniroad.backend.domain.notification.repository.NotificationSettingRepository;
import com.uniroad.backend.domain.notification.repository.NotificationRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostCommentRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostLikeRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.domain.ticket.repository.TicketTransferRepository;
import com.uniroad.backend.domain.verification.repository.VerificationRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import com.uniroad.backend.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FreePostCommentRepository freePostCommentRepository;
    private final FreePostLikeRepository freePostLikeRepository;
    private final FreePostRepository freePostRepository;
    private final CompanionPostRepository companionPostRepository;
    private final UsedItemRepository usedItemRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final AccountBookRepository accountBookRepository;

    public MemberResponseDto getMyInfo() {
        return MemberResponseDto.from(getCurrentMember());
    }

    // TODO(보안): 현재 비밀번호 확인이 없어, 토큰이 한 번 유출되면 공격자가 곧바로
    //   비밀번호를 바꿔 계정을 영구히 가져갈 수 있다. 반대로 사용자가 비밀번호를 바꿔도
    //   이미 발급된 토큰이 살아 있어 공격자를 쫓아내지 못한다.
    //   요청에 currentPassword를 추가하고 변경 후 member.invalidateIssuedTokens()로
    //   토큰을 회수하는 것이 맞다. 프론트엔드 계약이 함께 바뀌므로 협의 후 적용한다.
    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        Member member = getCurrentMember();
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteMyAccount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = getCurrentMember();

        refreshTokenRepository.deleteByMemberId(memberId);
        verificationRepository.deleteByMemberId(memberId);
        memberSocialAccountRepository.deleteByMemberId(memberId);
        notificationRepository.deleteByUserId(memberId);
        fcmTokenRepository.deleteByMemberId(memberId);
        notificationSettingRepository.deleteByMemberId(memberId);
        chatRoomMemberRepository.deleteByMemberId(memberId);
        chatMessageRepository.deleteBySenderId(memberId);
        freePostCommentRepository.deleteByMemberId(memberId);
        freePostLikeRepository.deleteByMemberId(memberId);
        freePostRepository.deleteByMemberId(memberId);
        companionPostRepository.deleteByMemberId(memberId);
        usedItemRepository.deleteByAuthorId(memberId);
        ticketTransferRepository.deleteByAuthorId(memberId);
        accountBookRepository.deleteByMemberId(memberId);

        memberRepository.delete(member);
    }

    @Transactional
    public MemberResponseDto updateMyProfile(MemberProfileUpdateRequest request) {
        Member member = getCurrentMember();
        University domesticUniversity = findOrCreateUniversity(request.domesticUniversity());

        member.updateProfile(
                request.currentSituation(),
                normalizeOptional(request.nickname()),
                normalizeOptional(request.dispatchedUniversity()),
                normalizeOptional(request.dispatchedCountry()),
                normalizeOptional(request.dispatchedRegion()),
                domesticUniversity,
                request.dispatchYear(),
                normalizeOptional(request.dispatchSemester()),
                request.applicationDeadline(),
                request.departureDate(),
                request.dispatchStartDate(),
                request.returnDate()
        );

        return MemberResponseDto.from(member);
    }

    private Member getCurrentMember() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private University findOrCreateUniversity(String universityName) {
        String normalizedName = normalizeOptional(universityName);
        if (normalizedName == null) {
            return null;
        }

        return universityRepository.findByName(normalizedName)
                .orElseGet(() -> universityRepository.save(
                        University.builder()
                                .name(normalizedName)
                                .build()
                ));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return normalized;
    }
}
