package com.uniroad.backend.domain.member.dto;

import com.uniroad.backend.domain.member.entity.Member;
import com.uniroad.backend.domain.member.entity.MemberStatus;
import com.uniroad.backend.domain.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MemberResponseDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Integer age;
    private String dispatchedUniversity;
    private String dispatchedCountry;
    private String dispatchedRegion;
    private Role role;
    private MemberStatus status;
    private BigDecimal balance;
    
    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .name(member.getName())
                .age(member.getAge())
                .dispatchedUniversity(member.getDispatchedUniversity())
                .dispatchedCountry(member.getDispatchedCountry())
                .dispatchedRegion(member.getDispatchedRegion())
                .role(member.getRole())
                .status(member.getStatus())
                .balance(member.getBalance())
                .build();
    }
}
