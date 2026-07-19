package com.uniroad.backend.domain.member.service;

import com.uniroad.backend.domain.info.entity.University;
import com.uniroad.backend.domain.info.repository.UniversityRepository;
import com.uniroad.backend.domain.accountbook.repository.AccountBookRepository;
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
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
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

    @Transactional
    public void updatePassword(PasswordUpdateRequest request) {
        Member member = getCurrentMember();
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteMyAccount() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = getCurrentMember();

        verificationRepository.deleteByMemberId(memberId);
        memberSocialAccountRepository.deleteByMemberId(memberId);
        notificationRepository.deleteByUserId(memberId);
        fcmTokenRepository.deleteByMemberId(memberId);
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
