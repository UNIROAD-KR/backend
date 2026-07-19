package com.uniroad.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // Auth
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    ALREADY_SIGNED_UP(HttpStatus.BAD_REQUEST, "이미 가입이 완료된 계정입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "토큰 정보가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다."),
    INVALID_OAUTH2_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 소셜 액세스 토큰입니다."),
    OAUTH2_USER_INFO_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 사용자 정보를 불러올 수 없습니다."),

    // Post
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공지사항입니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신고입니다."),
    INVALID_REPORT_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 신고 상태입니다."),

    // Used item
    USED_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 중고거래 게시글입니다."),
    USED_ITEM_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 판매 완료된 게시글입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    STOP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정류장 정보를 찾을 수 없습니다."),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "기록을 찾을 수 없습니다."),

    // Chat
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방 접근 권한이 없습니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방 참여자를 찾을 수 없습니다."),
    INVALID_CHAT_MESSAGE(HttpStatus.BAD_REQUEST, "유효하지 않은 채팅 메시지입니다."),
    CANNOT_CHAT_WITH_SELF(HttpStatus.BAD_REQUEST, "자기 자신과는 채팅할 수 없습니다."),

    // Info
    PARTNER_UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파견교입니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 후기입니다."),
    SCHOLARSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장학금입니다."),
    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 학교입니다."),
    EXCHANGE_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "교환학생 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
