package com.nextstep.nextsiheung.global.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

// 명세의 날짜·D-day는 모두 한국 시간(KST) 기준
public final class Kst {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private Kst() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    // 명세: D-day는 서버가 계산해 정수로 준다. 지났으면 음수, 날짜가 없으면 null
    public static Integer dDay(LocalDate date) {
        return date == null ? null : (int) ChronoUnit.DAYS.between(today(), date);
    }
}
