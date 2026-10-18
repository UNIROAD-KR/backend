package com.uniroad.backend.domain.community;

import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentRequest;
import com.uniroad.backend.domain.community.freepost.dto.FreePostCommentResponse;
import com.uniroad.backend.domain.community.freepost.dto.FreePostDetailResponse;
import com.uniroad.backend.domain.community.freepost.entity.FreePost;
import com.uniroad.backend.domain.community.freepost.repository.FreePostCommentRepository;
import com.uniroad.backend.domain.community.freepost.repository.FreePostRepository;
import com.uniroad.backend.domain.community.freepost.service.FreePostService;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FreePostCommentReplyTest {

    @Autowired
    private FreePostService freePostService;
    @Autowired
    private FreePostRepository freePostRepository;
    @Autowired
    private FreePostCommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager entityManager;

    private Long writer;
    private Long me;
    private Long other;
    private Long postId;

    @BeforeEach
    void setUp() {
        writer = member("writer");
        me = member("me");
        other = member("other");
        postId = freePostRepository.save(FreePost.builder()
                .member(memberRepository.getReferenceById(writer))
                .title("제목")
                .content("내용")
                .country("프랑스")
                .status("파견 중")
                .build()).getId();
    }

    @Test
    @DisplayName("댓글 하나에 답글이 여러 개 달리고, 상세는 댓글 아래에 답글을 묶으며 댓글 수는 답글까지 센다")
    void repliesAreNestedAndCounted() {
        Long first = comment(me, "첫 댓글", null);
        Long second = comment(other, "둘째 댓글", null);
        comment(other, "답글 1", first);
        comment(writer, "답글 2", first);
        sync();

        FreePostDetailResponse detail = freePostService.getPost(me, postId);

        assertThat(detail.commentCount()).isEqualTo(4);
        assertThat(detail.comments()).extracting(FreePostCommentResponse::id).containsExactly(first, second);
        FreePostCommentResponse firstComment = detail.comments().get(0);
        assertThat(firstComment.replies()).extracting(FreePostCommentResponse::content).containsExactly("답글 1", "답글 2");
        assertThat(firstComment.replies()).allSatisfy(reply -> {
            assertThat(reply.parentId()).isEqualTo(first);
            assertThat(reply.replies()).isEmpty();
        });
        assertThat(firstComment.mine()).isTrue();

        assertThat(freePostService.getPosts(null, null, 10, null, null).items().get(0).commentCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("답글에는 답글을 달 수 없고, 다른 글의 댓글에도 달 수 없다")
    void replyRules() {
        Long parent = comment(me, "댓글", null);
        Long reply = comment(other, "답글", parent);

        assertError(() -> comment(me, "답글의 답글", reply), ErrorCode.REPLY_TO_REPLY_NOT_ALLOWED);

        Long otherPost = freePostRepository.save(FreePost.builder()
                .member(memberRepository.getReferenceById(writer))
                .title("다른 글").content("내용").country("프랑스").status("파견 중")
                .build()).getId();
        assertError(() -> freePostService.createComment(me, otherPost, new FreePostCommentRequest("엉뚱한 답글", parent)),
                ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("답글이 있는 댓글을 지우면 '삭제된 댓글입니다'로 남고 수에서 빠지며, 마지막 답글이 지워지면 자리도 사라진다")
    void deletingParentWithReplies() {
        Long parent = comment(me, "댓글", null);
        Long reply1 = comment(other, "답글 1", parent);
        Long reply2 = comment(writer, "답글 2", parent);
        sync();

        freePostService.deleteComment(me, postId, parent);
        sync();

        FreePostDetailResponse detail = freePostService.getPost(me, postId);
        assertThat(detail.commentCount()).isEqualTo(2);
        FreePostCommentResponse placeholder = detail.comments().get(0);
        assertThat(placeholder.deleted()).isTrue();
        assertThat(placeholder.content()).isEqualTo("삭제된 댓글입니다");
        assertThat(placeholder.mine()).isFalse();
        assertThat(placeholder.replies()).hasSize(2);

        // 지워진 댓글은 고칠 수도, 다시 지울 수도, 답글을 달 수도 없다.
        assertError(() -> freePostService.updateComment(me, postId, parent, new FreePostCommentRequest("부활", null)),
                ErrorCode.COMMENT_NOT_FOUND);
        assertError(() -> freePostService.deleteComment(me, postId, parent), ErrorCode.COMMENT_NOT_FOUND);
        assertError(() -> comment(other, "새 답글", parent), ErrorCode.REPLY_TO_DELETED_COMMENT);

        freePostService.deleteComment(other, postId, reply1);
        sync();
        assertThat(commentRepository.findById(parent)).isPresent();

        freePostService.deleteComment(writer, postId, reply2);
        sync();
        assertThat(commentRepository.findById(parent)).isEmpty();
        assertThat(freePostService.getPost(me, postId).comments()).isEmpty();
        assertThat(freePostService.getPost(me, postId).commentCount()).isZero();
    }

    @Test
    @DisplayName("답글이 없는 댓글은 바로 지워진다")
    void deletingCommentWithoutReplies() {
        Long parent = comment(me, "댓글", null);
        sync();

        freePostService.deleteComment(me, postId, parent);
        sync();

        assertThat(commentRepository.findById(parent)).isEmpty();
    }

    @Test
    @DisplayName("답글이 달린 글을 지우거나 댓글 쓴 회원이 탈퇴해도 외래키에 걸리지 않는다")
    void bulkDeletesRespectReplyOrder() {
        Long parent = comment(me, "댓글", null);
        comment(other, "답글", parent);
        Long othersParent = comment(other, "남의 댓글", null);
        Long myReply = comment(me, "내 답글", othersParent);
        sync();

        commentRepository.deleteAllWrittenBy(me);
        sync();
        assertThat(commentRepository.findByFreePostIdOrderByIdAsc(postId))
                .extracting(c -> c.getId()).containsExactly(othersParent);
        assertThat(commentRepository.findById(myReply)).isEmpty();

        comment(writer, "답글", othersParent);
        sync();
        freePostService.deletePost(writer, postId);
        sync();
        assertThat(commentRepository.findByFreePostIdOrderByIdAsc(postId)).isEmpty();
    }

    private Long comment(Long memberId, String content, Long parentId) {
        return freePostService.createComment(memberId, postId, new FreePostCommentRequest(content, parentId)).id();
    }

    private void assertError(ThrowingCallable call, ErrorCode errorCode) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getCode())
                .isEqualTo(errorCode.name());
    }

    private void sync() {
        entityManager.flush();
        entityManager.clear();
    }

    private Long member(String key) {
        return memberRepository.save(Member.builder()
                .email(key + "@comment.test")
                .password("password")
                .name(key)
                .role(Role.VERIFIED)
                .provider("LOCAL")
                .build()).getId();
    }
}
