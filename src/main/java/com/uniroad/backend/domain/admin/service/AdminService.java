package com.uniroad.backend.domain.admin.service;

import com.uniroad.backend.domain.accountbook.repository.AccountBookRepository;
import com.uniroad.backend.domain.chat.repository.ChatMessageRepository;
import com.uniroad.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostCommentRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostLikeRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.companion.repository.CompanionPostRepository;
import com.uniroad.backend.domain.admin.dto.AdminDashboardResponse;
import com.uniroad.backend.domain.info.repository.FavoritePartnerUniversityRepository;
import com.uniroad.backend.domain.info.repository.ReviewCommentRepository;
import com.uniroad.backend.domain.info.repository.ReviewLikeRepository;
import com.uniroad.backend.domain.info.repository.ReviewRepository;
import com.uniroad.backend.domain.info.repository.UniversityExchangeDocumentCheckRepository;
import com.uniroad.backend.domain.member.dto.MemberResponseDto;
import com.uniroad.backend.domain.member.dto.MemberRoleUpdateRequest;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.domain.member.repository.MemberSocialAccountRepository;
import com.uniroad.backend.domain.notification.repository.FcmTokenRepository;
import com.uniroad.backend.domain.notification.repository.NotificationRepository;
import com.uniroad.backend.domain.notice.repository.NoticeRepository;
import com.uniroad.backend.domain.report.service.ReportService;
import com.uniroad.backend.domain.useditem.repository.UsedItemRepository;
import com.uniroad.backend.domain.verification.dto.AdminVerificationResponse;
import com.uniroad.backend.domain.verification.repository.VerificationRepository;
import com.uniroad.backend.domain.verification.service.VerificationService;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final VerificationRepository verificationRepository;
    private final VerificationService verificationService;
    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FreePostRepository freePostRepository;
    private final FreePostCommentRepository freePostCommentRepository;
    private final FreePostLikeRepository freePostLikeRepository;
    private final CompanionPostRepository companionPostRepository;
    private final UsedItemRepository usedItemRepository;
    private final com.uniroad.backend.domain.ticket.repository.TicketTransferRepository ticketTransferRepository;
    private final AccountBookRepository accountBookRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UniversityExchangeDocumentCheckRepository universityExchangeDocumentCheckRepository;
    private final FavoritePartnerUniversityRepository favoritePartnerUniversityRepository;
    private final ReportService reportService;

    public List<MemberResponseDto> getMembers() {
        return memberRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(MemberResponseDto::from)
                .toList();
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = findMember(memberId);

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
        accountBookRepository.deleteByMemberId(memberId);
        reviewCommentRepository.deleteByMemberId(memberId);
        reviewLikeRepository.deleteByMemberId(memberId);
        reviewRepository.deleteByMemberId(memberId);
        universityExchangeDocumentCheckRepository.deleteByMemberId(memberId);
        favoritePartnerUniversityRepository.deleteByMemberId(memberId);

        memberRepository.delete(member);
    }

    @Transactional
    public MemberResponseDto updateMemberRole(Long memberId, MemberRoleUpdateRequest request) {
        Member member = findMember(memberId);
        member.updateRole(request.role());
        return MemberResponseDto.from(member);
    }

    public List<AdminVerificationResponse> getApprovedVerifications() {
        return verificationService.getApprovedVerifications();
    }

    public List<AdminVerificationResponse> getRejectedVerifications() {
        return verificationService.getRejectedVerifications();
    }

    public AdminDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        long todaySignups = memberRepository.findAll().stream()
                .filter(member -> member.getCreatedAt() != null && member.getCreatedAt().toLocalDate().equals(today))
                .count();

        long totalPosts = freePostRepository.count()
                + usedItemRepository.count()
                + companionPostRepository.count()
                + ticketTransferRepository.count();

        return AdminDashboardResponse.builder()
                .totalMembers(memberRepository.count())
                .todaySignups(todaySignups)
                .totalPosts(totalPosts)
                .pendingVerifications(verificationRepository.countByStatusAndIsCurrentTrue(com.uniroad.backend.domain.verification.entity.VerificationStatus.PENDING))
                .reportCount(reportService.countAll())
                .resolvedReportCount(reportService.countResolved())
                .pendingReportCount(reportService.countPending())
                .build();
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        if (!noticeRepository.existsById(noticeId)) {
            throw new CustomException(ErrorCode.NOTICE_NOT_FOUND);
        }
        noticeRepository.deleteById(noticeId);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
