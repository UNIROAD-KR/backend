package com.uniroad.backend.domain.blog.service;

import com.uniroad.backend.domain.blog.dto.BlogPostDetailResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostLikeResponse;
import com.uniroad.backend.domain.blog.dto.BlogPostRequest;
import com.uniroad.backend.domain.blog.dto.BlogPostSummaryResponse;
import com.uniroad.backend.domain.blog.entity.BlogPost;
import com.uniroad.backend.domain.blog.entity.BlogPostContent;
import com.uniroad.backend.domain.blog.entity.BlogPostLike;
import com.uniroad.backend.domain.blog.entity.BlogPostSeo;
import com.uniroad.backend.domain.blog.entity.BlogPostStatus;
import com.uniroad.backend.domain.blog.repository.BlogPostLikeRepository;
import com.uniroad.backend.domain.blog.repository.BlogPostRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.common.CursorPageResponse;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogPostService {

    private static final int SUMMARY_LENGTH = 150;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TAGS = 10;

    private final BlogPostRepository blogPostRepository;
    private final BlogPostLikeRepository blogPostLikeRepository;
    private final MemberRepository memberRepository;
    private final BlogContentSanitizer sanitizer;

    // ── 공개 조회 ─────────────────────────────────────────────

    /** memberId는 비로그인 방문자면 null이다 — 그때는 likedByMe가 항상 false다 */
    public CursorPageResponse<BlogPostSummaryResponse> getPublishedPosts(Long cursorId, int size, Long memberId) {
        int pageSize = normalizeSize(size);

        // hasNext를 알아내려고 한 개 더 읽는다
        List<BlogPost> posts = blogPostRepository.findPublishedByCursor(
                BlogPostStatus.PUBLISHED, cursorId, PageRequest.of(0, pageSize + 1));

        boolean hasNext = posts.size() > pageSize;
        List<BlogPost> pageItems = hasNext ? posts.subList(0, pageSize) : posts;

        List<BlogPostSummaryResponse> items = toSummaries(pageItems, memberId);
        Long nextCursorId = hasNext && !pageItems.isEmpty()
                ? pageItems.get(pageItems.size() - 1).getId()
                : null;

        return new CursorPageResponse<>(items, nextCursorId, hasNext);
    }

    @Transactional
    public BlogPostDetailResponse getPublishedPost(String slug, Long memberId) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(ErrorCode.BLOG_POST_NOT_FOUND));

        // 초안은 공개 경로로 열리면 안 된다. 존재 자체를 숨기려고 404로 돌려준다.
        if (!post.isPublished()) {
            throw new CustomException(ErrorCode.BLOG_POST_NOT_FOUND);
        }

        // 엔티티를 고치지 않고 UPDATE 한 문장으로 올린다 — updatedAt을 건드리면 안 된다(레포지토리 주석 참고).
        // 엔티티에는 반영되지 않으므로 이번 조회까지 센 값을 따로 넘긴다.
        blogPostRepository.increaseViewCount(post.getId());
        return toDetail(post, memberId, false, post.getViewCount() + 1);
    }

    // ── 좋아요 ────────────────────────────────────────────────

    @Transactional
    public BlogPostLikeResponse toggleLike(Long postId, Long memberId) {
        BlogPost post = findPost(postId);
        if (!post.isPublished()) {
            throw new CustomException(ErrorCode.BLOG_POST_NOT_FOUND);
        }

        blogPostLikeRepository.findByBlogPostIdAndMemberId(postId, memberId)
                .ifPresentOrElse(
                        blogPostLikeRepository::delete,
                        () -> {
                            Member member = memberRepository.findById(memberId)
                                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
                            blogPostLikeRepository.save(
                                    BlogPostLike.builder().blogPost(post).member(member).build());
                        });

        // 방금의 저장/삭제가 반영된 수를 읽어야 하므로 flush 후에 센다
        blogPostLikeRepository.flush();
        boolean liked = blogPostLikeRepository.existsByBlogPostIdAndMemberId(postId, memberId);
        return new BlogPostLikeResponse(postId, liked, blogPostLikeRepository.countByBlogPostId(postId));
    }

    // ── 관리자 ────────────────────────────────────────────────

    public List<BlogPostSummaryResponse> getAllPostsForAdmin() {
        return toSummaries(blogPostRepository.findAllByOrderByIdDesc(), null);
    }

    /** 수정 화면용 — 에디터 복원에 필요한 contentJson까지 함께 내려준다 */
    public BlogPostDetailResponse getPostForAdmin(Long postId) {
        return toDetail(findPost(postId), null, true);
    }

    @Transactional
    public BlogPostDetailResponse createPost(BlogPostRequest request, Long authorId) {
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String sanitizedHtml = sanitizer.sanitize(request.contentHtml());
        String plainText = sanitizer.toPlainText(sanitizedHtml);
        String desiredSlug = resolveSlug(request.slug(), request.title());

        BlogPost post = BlogPost.builder()
                .content(new BlogPostContent(
                        // slug를 못 만든 경우 임시 값으로 저장한 뒤 id로 바꾼다
                        desiredSlug == null ? temporarySlug() : uniqueSlug(desiredSlug, null),
                        request.title().trim(),
                        resolveSummary(request.summary(), plainText),
                        resolveThumbnail(request.thumbnailUrl(), sanitizedHtml),
                        request.contentJson(),
                        sanitizedHtml,
                        plainText
                ))
                .seo(toSeo(request))
                .status(request.published() ? BlogPostStatus.PUBLISHED : BlogPostStatus.DRAFT)
                .author(author)
                .build();

        BlogPost saved = blogPostRepository.saveAndFlush(post);
        if (desiredSlug == null) {
            saved.assignSlug(uniqueSlug("post-" + saved.getId(), saved.getId()));
        }

        return toDetail(saved, null, true);
    }

    @Transactional
    public BlogPostDetailResponse updatePost(Long postId, BlogPostRequest request) {
        BlogPost post = findPost(postId);

        String sanitizedHtml = sanitizer.sanitize(request.contentHtml());
        String plainText = sanitizer.toPlainText(sanitizedHtml);
        String desiredSlug = resolveSlug(request.slug(), request.title());
        String slug = desiredSlug == null ? post.getSlug() : uniqueSlug(desiredSlug, postId);

        post.update(
                new BlogPostContent(
                        slug,
                        request.title().trim(),
                        resolveSummary(request.summary(), plainText),
                        resolveThumbnail(request.thumbnailUrl(), sanitizedHtml),
                        request.contentJson(),
                        sanitizedHtml,
                        plainText
                ),
                toSeo(request)
        );

        if (request.published()) {
            post.publish();
        } else {
            post.unpublish();
        }

        return toDetail(post, null, true);
    }

    @Transactional
    public BlogPostDetailResponse setPublished(Long postId, boolean published) {
        BlogPost post = findPost(postId);
        if (published) {
            post.publish();
        } else {
            post.unpublish();
        }
        return toDetail(post, null, false);
    }

    @Transactional
    public void deletePost(Long postId) {
        BlogPost post = findPost(postId);
        // 좋아요가 남아 있으면 FK 제약에 걸린다
        blogPostLikeRepository.deleteAllByBlogPostId(postId);
        blogPostRepository.delete(post);
    }

    // ── 내부 ──────────────────────────────────────────────────

    private BlogPost findPost(Long postId) {
        return blogPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BLOG_POST_NOT_FOUND));
    }

    private int normalizeSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /** 목록의 좋아요 수와 내가 누른 여부를 글 개수와 무관하게 두 번의 질의로 채운다 */
    private List<BlogPostSummaryResponse> toSummaries(List<BlogPost> posts, Long memberId) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(BlogPost::getId).toList();
        Map<Long, Long> likeCounts = countLikes(postIds);
        Set<Long> likedIds = likedPostIds(postIds, memberId);

        List<BlogPostSummaryResponse> result = new ArrayList<>(posts.size());
        for (BlogPost post : posts) {
            result.add(BlogPostSummaryResponse.of(
                    post,
                    likeCounts.getOrDefault(post.getId(), 0L),
                    likedIds.contains(post.getId())
            ));
        }
        return result;
    }

    private BlogPostDetailResponse toDetail(BlogPost post, Long memberId, boolean includeJson) {
        return toDetail(post, memberId, includeJson, post.getViewCount());
    }

    private BlogPostDetailResponse toDetail(BlogPost post, Long memberId, boolean includeJson, long viewCount) {
        long likeCount = blogPostLikeRepository.countByBlogPostId(post.getId());
        boolean likedByMe = memberId != null
                && blogPostLikeRepository.existsByBlogPostIdAndMemberId(post.getId(), memberId);
        return BlogPostDetailResponse.of(post, likeCount, likedByMe, includeJson, viewCount);
    }

    private Map<Long, Long> countLikes(Collection<Long> postIds) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : blogPostLikeRepository.countByBlogPostIds(postIds)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private Set<Long> likedPostIds(Collection<Long> postIds, Long memberId) {
        if (memberId == null) {
            return Set.of();
        }
        return new HashSet<>(blogPostLikeRepository.findLikedPostIds(memberId, postIds));
    }

    /**
     * SEO 값은 작성자가 적은 것만 담는다.
     * 프론트는 빈 칸을 ""로 보내오므로 여기서 null로 눕혀, "비었다"의 표현을 한 가지로 고정한다.
     */
    private BlogPostSeo toSeo(BlogPostRequest request) {
        return new BlogPostSeo(
                blankToNull(request.metaTitle()),
                blankToNull(request.metaDescription()),
                blankToNull(request.ogImageUrl()),
                normalizeTags(request.tags()),
                blankToNull(request.canonicalUrl()),
                request.noindex()
        );
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 소문자로 눕히고 공백을 정리한 뒤 중복을 없앤다.
     * 다듬지 않으면 "교환학생"과 "교환학생 "이 서로 다른 태그가 되어 태그로 묶는 일이 불가능해진다.
     */
    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null) {
                continue;
            }
            String normalized = tag.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                unique.add(normalized);
            }
            if (unique.size() == MAX_TAGS) {
                break;
            }
        }
        return List.copyOf(unique);
    }

    private String resolveSummary(String summary, String plainText) {
        if (summary != null && !summary.isBlank()) {
            return summary.trim();
        }
        if (plainText.isEmpty()) {
            return "";
        }
        return plainText.length() <= SUMMARY_LENGTH
                ? plainText
                : plainText.substring(0, SUMMARY_LENGTH).trim() + "…";
    }

    private String resolveThumbnail(String thumbnailUrl, String html) {
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            return thumbnailUrl.trim();
        }
        return sanitizer.firstImageUrl(html);
    }

    /**
     * 입력한 slug를 먼저 쓰고, 없으면 제목에서 뽑는다.
     * 한글 제목은 남는 글자가 없어 null이 되며, 이때는 저장 후 id로 채운다.
     */
    private String resolveSlug(String requestedSlug, String title) {
        String source = (requestedSlug != null && !requestedSlug.isBlank()) ? requestedSlug : title;
        String normalized = normalizeSlug(source);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeSlug(String source) {
        if (source == null) {
            return "";
        }
        String slug = source.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.length() > 180 ? slug.substring(0, 180).replaceAll("-+$", "") : slug;
    }

    /** 이미 쓰는 slug면 -2, -3을 붙여 비켜 간다. selfId는 자기 자신을 충돌로 보지 않기 위한 값이다. */
    private String uniqueSlug(String base, Long selfId) {
        String candidate = base;
        int suffix = 2;
        while (isSlugTaken(candidate, selfId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean isSlugTaken(String slug, Long selfId) {
        return blogPostRepository.findBySlug(slug)
                .filter(found -> !found.getId().equals(selfId))
                .isPresent();
    }

    private String temporarySlug() {
        return "draft-" + UUID.randomUUID();
    }
}
