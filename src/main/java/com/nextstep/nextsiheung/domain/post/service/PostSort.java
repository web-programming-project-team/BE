package com.nextstep.nextsiheung.domain.post.service;

import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import java.util.Locale;

// GET /posts 정렬. latest(기본, 최신순) · likes(도움돼요 많은순)
public enum PostSort {
    LATEST, LIKES;

    public static PostSort from(String value) {
        if (value == null || value.isBlank()) {
            return LATEST;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
