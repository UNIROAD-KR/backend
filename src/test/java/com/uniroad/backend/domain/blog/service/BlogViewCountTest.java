package com.uniroad.backend.domain.blog.service;

import com.uniroad.backend.domain.blog.dto.BlogPostDetailResponse;
import com.uniroad.backend.domain.blog.entity.BlogPost;
import com.uniroad.backend.domain.blog.entity.BlogPostContent;
import com.uniroad.backend.domain.blog.entity.BlogPostSeo;
import com.uniroad.backend.domain.blog.entity.BlogPostStatus;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 조회가 글의 수정 시각을 건드리지 않는지 확인한다.
 *
 * 예전에는 엔티티의 viewCount를 직접 올렸는데, 더티체킹이 @LastModifiedDate까지 함께 움직여
 * updatedAt이 사실상 "마지막 조회 시각"이 됐다. 그 값이 상세 페이지의 dateModified와
 * sitemap의 lastmod로 나가므로, 방문할 때마다 검색엔진에 "글이 방금 바뀌었다"고 알리는 셈이었다.
 */
@ExtendWith(MockitoExtension.class)
class BlogViewCountTest {

    @InjectMocks
    private BlogPostService blogPostService;

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogPostLikeRepository blogPostLikeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BlogContentSanitizer sanitizer;

    @Test
    @DisplayName("조회는 엔티티를 건드리지 않고 UPDATE 한 문장으로 조회수만 올린다")
    void viewDoesNotTouchTheEntity() {
        BlogPost post = publishedPost(41L);
        LocalDateTime before = post.getUpdatedAt();
        given(blogPostRepository.findBySlug("guide")).willReturn(Optional.of(post));

        blogPostService.getPublishedPost("guide", null);

        verify(blogPostRepository).increaseViewCount(1L);
        // 엔티티가 그대로여야 커밋 시점에 UPDATE가 생기지 않는다 — updatedAt이 움직이는 경로가 그것이다
        assertThat(post.getViewCount()).isEqualTo(41L);
        assertThat(post.getUpdatedAt()).isEqualTo(before);
    }

    @Test
    @DisplayName("응답의 조회수에는 이번 조회가 반영된다")
    void responseCountsTheCurrentView() {
        given(blogPostRepository.findBySlug("guide")).willReturn(Optional.of(publishedPost(41L)));

        BlogPostDetailResponse response = blogPostService.getPublishedPost("guide", null);

        assertThat(response.viewCount()).isEqualTo(42L);
    }

    private BlogPost publishedPost(long viewCount) {
        Member author = Member.builder()
                .email("admin@uniroad.kr")
                .name("운영자")
                .nickname("운영자")
                .role(Role.ADMIN)
                .build();

        BlogPost post = BlogPost.builder()
                .content(new BlogPostContent(
                        "guide", "제목", "요약", null, Map.of("type", "doc"), "<p>본문</p>", "본문"))
                .seo(BlogPostSeo.empty())
                .status(BlogPostStatus.PUBLISHED)
                .author(author)
                .build();

        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post, "viewCount", viewCount);
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        return post;
    }
}
