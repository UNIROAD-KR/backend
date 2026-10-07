package com.uniroad.backend.domain.community.freepost.dto;

public enum FreePostSort {
    /** 최신순 */
    LATEST,
    /** 오래된순 */
    OLDEST,
    /** 인기순 (좋아요 많은 순, 같으면 최신순) */
    POPULAR
}
