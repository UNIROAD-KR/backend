package com.uniroad.backend.domain.blog.service;

import com.uniroad.backend.domain.blog.dto.BlogPostDetailResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostRequest;
import com.uniroad.backend.domain.blog.entity.BlogPost;
import com.uniroad.backend.domain.blog.repository.BlogPostLikeRepository;
import com.uniroad.backend.domain.blog.repository.BlogPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.Role;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

/**
 * 검색 노출 값이 "작성자가 적은 것"과 "비었을 때 대신 쓰는 것"으로 나뉘어 있는지 확인한다.
 *
 * 이 구분이 무너지면 수정 화면이 자동 생성된 설명을 작성자가 쓴 것처럼 보여주고,
 * 그 시점부터 본문을 고쳐도 옛 설명이 그대로 남는다.
 */
@ExtendWith(MockitoExtension.class)
class BlogPostSeoTest {

    @InjectMocks
    private BlogPostService blogPostService;

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogPostLikeRepository blogPostLikeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Spy
    private BlogContentSanitizer sanitizer = new BlogContentSanitizer("https://uniroad.kr");

    /* ── fallback ──────────────────────────────────────── */

    @Test
    @DisplayName("SEO 값을 비우면 화면용 값으로 채워 내려주고, 저장된 값은 비어 있는 채로 둔다")
    void fallsBackToDisplayValuesWithoutStoringThem() {
        BlogPostDetailResponse response = create(request(
                "교환학생 준비 안내", "카드 설명입니다", "https://cdn.example.com/card.png",
                null, null, null, null, null, false));

        // 작성자가 적은 값은 비어 있다 — 수정 화면은 이걸 편집한다
        assertThat(response.metaTitle()).isNull();
        assertThat(response.metaDescription()).isNull();
        assertThat(response.ogImageUrl()).isNull();

        // 페이지가 실제로 쓰는 값은 채워져 있다
        assertThat(response.effectiveMetaTitle()).isEqualTo("교환학생 준비 안내");
        assertThat(response.effectiveMetaDescription()).isEqualTo("카드 설명입니다");
        assertThat(response.effectiveOgImageUrl()).isEqualTo("https://cdn.example.com/card.png");
    }

    @Test
    @DisplayName("SEO 값을 적으면 화면용 값 대신 그것을 쓴다")
    void prefersAuthoredSeoValues() {
        BlogPostDetailResponse response = create(request(
                "교환학생 준비 안내", "카드 설명입니다", "https://cdn.example.com/card.png",
                "검색용 제목", "검색용 설명", "https://cdn.example.com/og.png", null, null, false));

        assertThat(response.effectiveMetaTitle()).isEqualTo("검색용 제목");
        assertThat(response.effectiveMetaDescription()).isEqualTo("검색용 설명");
        assertThat(response.effectiveOgImageUrl()).isEqualTo("https://cdn.example.com/og.png");
    }

    @Test
    @DisplayName("빈 문자열은 null과 같게 다룬다 — 프론트가 빈 칸을 \"\"로 보내온다")
    void treatsBlankAsEmpty() {
        BlogPostDetailResponse response = create(request(
                "제목", "요약", null, "   ", "", "  ", null, "  ", false));

        assertThat(response.metaTitle()).isNull();
        assertThat(response.metaDescription()).isNull();
        assertThat(response.ogImageUrl()).isNull();
        assertThat(response.canonicalUrl()).isNull();
        assertThat(response.effectiveMetaTitle()).isEqualTo("제목");
    }

    /* ── 태그 ──────────────────────────────────────────── */

    @Test
    @DisplayName("태그는 소문자로 눕히고 공백을 정리한 뒤 중복을 없앤다")
    void normalizesTags() {
        BlogPostDetailResponse response = create(request(
                "제목", "요약", null, null, null, null,
                List.of("  교환학생 ", "교환학생", "Exchange", "EXCHANGE", "파견  준비", "", "   "),
                null, false));

        assertThat(response.tags()).containsExactly("교환학생", "exchange", "파견 준비");
    }

    @Test
    @DisplayName("태그는 10개까지만 저장한다")
    void capsTagCount() {
        List<String> many = List.of("t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11", "t12");

        BlogPostDetailResponse response = create(request(
                "제목", "요약", null, null, null, null, many, null, false));

        assertThat(response.tags()).hasSize(10).endsWith("t10");
    }

    @Test
    @DisplayName("태그를 보내지 않은 예전 글은 빈 목록으로 읽힌다")
    void missingTagsReadAsEmptyList() {
        BlogPostDetailResponse response = create(request(
                "제목", "요약", null, null, null, null, null, null, false));

        assertThat(response.tags()).isEmpty();
    }

    /* ── 색인 제외 ─────────────────────────────────────── */

    @Test
    @DisplayName("noindex는 적은 그대로 전달된다")
    void carriesNoindex() {
        assertThat(create(request("제목", "요약", null, null, null, null, null, null, true)).noindex()).isTrue();
        assertThat(create(request("제목", "요약", null, null, null, null, null, null, false)).noindex()).isFalse();
    }

    /* ── 도우미 ────────────────────────────────────────── */

    private BlogPostDetailResponse create(BlogPostRequest request) {
        Member author = author();
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(author));
        given(blogPostRepository.findBySlug(any())).willReturn(Optional.empty());
        given(blogPostRepository.saveAndFlush(any(BlogPost.class))).willAnswer(invocation -> {
            BlogPost saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        lenient().when(blogPostLikeRepository.countByBlogPostId(anyLong())).thenReturn(0L);

        return blogPostService.createPost(request, 1L);
    }

    private BlogPostRequest request(
            String title,
            String summary,
            String thumbnailUrl,
            String metaTitle,
            String metaDescription,
            String ogImageUrl,
            List<String> tags,
            String canonicalUrl,
            boolean noindex
    ) {
        return new BlogPostRequest(
                title,
                "test-slug",
                summary,
                thumbnailUrl,
                Map.of("type", "doc"),
                "<p>본문입니다</p>",
                metaTitle,
                metaDescription,
                ogImageUrl,
                tags,
                canonicalUrl,
                noindex,
                true
        );
    }

    private Member author() {
        Member member = Member.builder()
                .email("admin@uniroad.kr")
                .password("encoded")
                .name("운영자")
                .nickname("운영자")
                .role(Role.ADMIN)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }
}
