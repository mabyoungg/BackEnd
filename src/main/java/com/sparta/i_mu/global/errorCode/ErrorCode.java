package com.sparta.i_mu.global.errorCode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    TOKEN_INVALID(400_1, HttpStatus.BAD_REQUEST, "유효한 토큰이 아닙니다."),
    TOKEN_EXPIRED(400_2, HttpStatus.BAD_REQUEST, "토큰이 만료되었습니다."),
    USER_NOT_AUTHENTICATED(400_3, HttpStatus.BAD_REQUEST, "인증된 사용자가 아닙니다."),
    POST_NOT_EXIST(400_4, HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    COMMENT_NOT_EXIST(400_5, HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    USER_NOT_MATCH(400_6, HttpStatus.BAD_REQUEST, "작성자만 수정, 삭제가 가능합니다."),
    FILE_INVALID(400_7,HttpStatus.BAD_REQUEST, "유효한 파일이 아닙니다."),
    FILE_DECODE_FAIL(400_8,HttpStatus.BAD_REQUEST, "파일 이름 디코딩에 실패했습니다."),
    URL_INVALID(400_9, HttpStatus.BAD_REQUEST, "잘못된 URL 형식입니다."),
    EXTRACT_INVALID(400_10, HttpStatus.BAD_REQUEST, "확장자를 추출할 수 없습니다.");


    private final int errorCode;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int errorCode, HttpStatus status, String message) {
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
    }

    }
