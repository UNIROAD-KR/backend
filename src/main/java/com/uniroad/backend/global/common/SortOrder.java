package com.uniroad.backend.global.common;

import org.springframework.data.domain.Sort;

public enum SortOrder {
    /** 최신순 */
    LATEST,
    /** 오래된순 */
    OLDEST;

    public boolean isOldest() {
        return this == OLDEST;
    }

    public Sort byId() {
        return Sort.by(isOldest() ? Sort.Direction.ASC : Sort.Direction.DESC, "id");
    }
}
