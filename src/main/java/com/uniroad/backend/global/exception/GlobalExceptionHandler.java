package com.uniroad.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
                        (existing, replacement) -> existing
                ));

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                "INVALID_INPUT_VALUE",
                "입력값 검증에 실패했습니다.",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 요청 본문을 읽지 못한 경우 (JSON 문법 오류, 잘못된 인코딩, enum 값 불일치 등)
     *
     * 클라이언트가 잘못 보낸 것이므로 500이 아니라 400으로 응답한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("[HttpMessageNotReadableException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                "INVALID_INPUT_VALUE",
                "요청 본문을 읽을 수 없습니다. 형식과 인코딩(UTF-8)을 확인해주세요."
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 필수 쿼리 파라미터가 빠진 경우
     *
     * 클라이언트가 잘못 보낸 것이므로 500이 아니라 400으로 응답한다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(MissingServletRequestParameterException ex) {
        log.warn("[MissingServletRequestParameterException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                "INVALID_INPUT_VALUE",
                "필수 파라미터 '" + ex.getParameterName() + "'가 필요합니다."
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 경로 변수나 쿼리 파라미터를 선언한 타입으로 바꾸지 못한 경우
     *
     * 예: enum 경로 변수에 없는 값(/settings/categories/UNKNOWN), 숫자 자리에 문자.
     * 클라이언트가 잘못 보낸 것이므로 500이 아니라 400으로 응답한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("[MethodArgumentTypeMismatchException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                "INVALID_INPUT_VALUE",
                "'" + ex.getName() + "' 값이 올바르지 않습니다."
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 해당 경로가 지원하지 않는 HTTP 메서드로 호출된 경우
     *
     * 클라이언트가 잘못 보낸 것이므로 500이 아니라 405로 응답한다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("[HttpRequestMethodNotSupportedException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                405,
                "METHOD_NOT_ALLOWED",
                "지원하지 않는 요청 방식입니다."
        );
        return ResponseEntity.status(405).body(response);
    }

    /**
     * 잘못된 파라미터로 서비스 로직이 거부한 경우
     *
     * 예: 지원하지 않는 fileType, contentType과 fileType 불일치
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("[IllegalArgumentException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                "INVALID_INPUT_VALUE",
                ex.getMessage() != null ? ex.getMessage() : ErrorCode.INVALID_INPUT_VALUE.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 커스텀 비즈니스 예외
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.warn("[CustomException] code={}, message={}", ex.getCode(), ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getCode(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Spring Security 인증 실패
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("[AuthenticationException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(401, "UNAUTHORIZED", ex.getMessage());
        return ResponseEntity.status(401).body(response);
    }

    /**
     * Spring Security 권한 없음
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("[AccessDeniedException] {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(403, "FORBIDDEN", "접근 권한이 없습니다.");
        return ResponseEntity.status(403).body(response);
    }

    /**
     * 예상치 못한 서버 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("[UnhandledException]", ex);
        ErrorResponse response = ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(response);
    }

    // ── 내부 응답 포맷 ──────────────────────────────────────
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String code,
            String message,
            Object errors
    ) {
        public static ErrorResponse of(int status, String code, String message) {
            return new ErrorResponse(LocalDateTime.now(), status, code, message, null);
        }

        public static ErrorResponse of(int status, String code, String message, Object errors) {
            return new ErrorResponse(LocalDateTime.now(), status, code, message, errors);
        }
    }
}
