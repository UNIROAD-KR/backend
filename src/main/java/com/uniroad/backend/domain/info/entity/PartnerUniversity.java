package com.uniroad.backend.domain.info.entity;

import com.uniroad.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "partner_university", indexes = {
        @Index(name = "idx_partner_university_country", columnList = "country"),
        @Index(name = "idx_partner_university_name", columnList = "name"),
        @Index(name = "idx_partner_university_dormitory", columnList = "dormitory_available")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PartnerUniversity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    private String city;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String websiteUrl;
    private String thumbnailUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> classLanguages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> supportedMajors;

    private Boolean creditTransferPossible;
    private String internationalOfficeEmail;
    private String internationalOfficeSnsUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal minGpa;

    private String languageRequirement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> requiredDocuments;

    private Boolean dormitoryAvailable;
    private String dormitoryType;
    private Integer dormitoryPrice;

    @Column(columnDefinition = "TEXT")
    private String housingDescription;

    @Column(columnDefinition = "TEXT")
    private String nearbyEnvironment;

    private Integer rentAvg;
    private Integer mealAvg;
    private Integer transportAvg;

    @Builder.Default
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer reviewCount = 0;
}
