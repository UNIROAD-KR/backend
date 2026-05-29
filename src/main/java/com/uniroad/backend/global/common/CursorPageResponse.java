package com.uniroad.backend.global.common;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursorId,
        boolean hasNext
) {
}
