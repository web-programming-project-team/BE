package com.nextstep.nextsiheung.global.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

// 복수 선택 값(@ElementCollection Set)을 응답 목록으로 바꿀 때 쓴다
// DB에서 읽은 Set은 순서가 보장되지 않아 요청마다 순서가 바뀔 수 있으므로 enum 선언 순서로 정렬한다
public final class EnumLists {

    private EnumLists() {
    }

    public static <E extends Enum<E>> List<E> sorted(Collection<E> values) {
        return values.stream().sorted(Comparator.comparingInt(Enum::ordinal)).toList();
    }
}
