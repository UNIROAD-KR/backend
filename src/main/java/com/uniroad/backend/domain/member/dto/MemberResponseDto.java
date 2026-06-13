package com.uniroad.backend.domain.member.dto;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.CurrentSituation;
import com.uniroad.backend.domain.member.entity.Gender;
import com.uniroad.backend.domain.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class MemberResponseDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private Gender gender;
    private CurrentSituation currentSituation;
    private Integer age;
    private Long domesticUniversityId;
    private String domesticUniversity;
    private String homeUniversity;
    private String dispatchedUniversity;
    private String dispatchedCountry;
    private String dispatchedRegion;
    private Integer dispatchYear;
    private String dispatchSemester;
    private LocalDate applicationDeadline;
    private LocalDate departureDate;
    private LocalDate dispatchStartDate;
    private LocalDate returnDate;
    private Role role;
    private MemberStatus status;
    private BigDecimal balance;
    
    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .gender(member.getGender())
                .currentSituation(member.getCurrentSituation())
                .age(member.getAge())
                .domesticUniversityId(member.getDomesticUniversity() != null ? member.getDomesticUniversity().getId() : null)
                .domesticUniversity(member.getDomesticUniversity() != null ? member.getDomesticUniversity().getName() : null)
                .homeUniversity(member.getDomesticUniversity() != null ? member.getDomesticUniversity().getName() : null)
                .dispatchedUniversity(member.getDispatchedUniversity())
                .dispatchedCountry(member.getDispatchedCountry())
                .dispatchedRegion(member.getDispatchedRegion())
                .dispatchYear(member.getDispatchYear())
                .dispatchSemester(member.getDispatchSemester())
                .applicationDeadline(member.getApplicationDeadline())
                .departureDate(member.getDepartureDate())
                .dispatchStartDate(member.getDispatchStartDate())
                .returnDate(member.getReturnDate())
                .role(member.getRole())
                .status(member.getStatus())
                .balance(member.getBalance())
                .build();
    }
}
