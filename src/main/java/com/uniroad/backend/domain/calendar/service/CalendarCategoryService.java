package com.uniroad.backend.domain.calendar.service;

import com.uniroad.backend.domain.calendar.dto.CalendarCategoryRequest;
import com.uniroad.backend.domain.calendar.dto.CalendarCategoryResponse;
import com.uniroad.backend.domain.calendar.dto.CalendarCategoryUpdateRequest;
import com.uniroad.backend.domain.calendar.entity.CalendarCategory;
import com.uniroad.backend.domain.calendar.repository.CalendarCategoryRepository;
import com.uniroad.backend.domain.calendar.repository.CalendarEventRepository;
import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.repository.MemberRepository;
import com.uniroad.backend.global.exception.CustomException;
import com.uniroad.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarCategoryService {

    private static final int MAX_CATEGORIES = 30;

    private record DefaultCategory(String name, String color) {
    }

    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            new DefaultCategory("수업", "#4F8EF7"),
            new DefaultCategory("여행", "#34C759"),
            new DefaultCategory("약속", "#FF9500"),
            new DefaultCategory("기타", "#8E8E93")
    );

    private final CalendarCategoryRepository categoryRepository;
    private final CalendarEventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * 카테고리가 하나도 없는 회원에게는 기본 카테고리를 만들어 준다.
     *
     * 가입할 때가 아니라 처음 조회할 때 만든다. 캘린더를 쓰지 않는 회원에게까지 행을 만들 이유가 없고,
     * 이 기능 이전에 가입한 회원도 같은 경로로 받게 된다.
     * 그래서 카테고리를 전부 지운 회원도 다음 조회에서 기본값을 다시 받는다.
     */
    public List<CalendarCategoryResponse> getCategories(Long memberId) {
        if (!categoryRepository.existsByMemberId(memberId)) {
            try {
                transactionTemplate.executeWithoutResult(status -> createDefaults(memberId));
            } catch (DataIntegrityViolationException e) {
                // 동시에 들어온 다른 요청이 먼저 만들었다. 아래 조회가 그 결과를 읽는다.
            }
        }

        return categoryRepository.findByMemberIdOrderBySortOrderAscIdAsc(memberId).stream()
                .map(CalendarCategoryResponse::from)
                .toList();
    }

    @Transactional
    public CalendarCategoryResponse createCategory(Long memberId, CalendarCategoryRequest request) {
        Member member = findMember(memberId);
        String name = request.name().trim();

        if (categoryRepository.countByMemberId(memberId) >= MAX_CATEGORIES) {
            throw new CustomException(ErrorCode.CALENDAR_CATEGORY_LIMIT_EXCEEDED);
        }
        if (categoryRepository.existsByMemberIdAndName(memberId, name)) {
            throw new CustomException(ErrorCode.DUPLICATE_CALENDAR_CATEGORY);
        }

        CalendarCategory category = categoryRepository.save(CalendarCategory.builder()
                .member(member)
                .name(name)
                .color(request.color())
                .sortOrder(categoryRepository.findMaxSortOrder(memberId) + 1)
                .build());

        return CalendarCategoryResponse.from(category);
    }

    @Transactional
    public CalendarCategoryResponse updateCategory(Long memberId, Long categoryId, CalendarCategoryUpdateRequest request) {
        CalendarCategory category = findCategory(memberId, categoryId);

        String name = request.name() != null ? request.name().trim() : null;
        if (name != null) {
            if (name.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
            if (!name.equals(category.getName()) && categoryRepository.existsByMemberIdAndName(memberId, name)) {
                throw new CustomException(ErrorCode.DUPLICATE_CALENDAR_CATEGORY);
            }
        }

        category.update(name, request.color());
        return CalendarCategoryResponse.from(category);
    }

    /** 카테고리를 지워도 일정은 남기고 미분류로 돌린다. */
    @Transactional
    public void deleteCategory(Long memberId, Long categoryId) {
        findCategory(memberId, categoryId);

        eventRepository.clearCategory(categoryId);
        categoryRepository.deleteById(categoryId);
    }

    private void createDefaults(Long memberId) {
        Member member = findMember(memberId);
        for (int i = 0; i < DEFAULT_CATEGORIES.size(); i++) {
            DefaultCategory defaults = DEFAULT_CATEGORIES.get(i);
            categoryRepository.save(CalendarCategory.builder()
                    .member(member)
                    .name(defaults.name())
                    .color(defaults.color())
                    .sortOrder(i)
                    .build());
        }
    }

    private CalendarCategory findCategory(Long memberId, Long categoryId) {
        return categoryRepository.findByIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CALENDAR_CATEGORY_NOT_FOUND));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
