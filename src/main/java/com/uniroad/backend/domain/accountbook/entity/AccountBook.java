package com.uniroad.backend.domain.accountbook.entity;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "account_book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AccountBook extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountCategory category;

    @Column(nullable = false)
    private String title;

    /**
     * 메모. 화면에서 삭제되어 더 이상 입력받지도, 응답에 싣지도 않는다.
     * 예전에 저장된 내용이 남아 있어 컬럼과 필드는 지우지 않는다.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate;
}
