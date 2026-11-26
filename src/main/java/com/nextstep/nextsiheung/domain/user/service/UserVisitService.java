package com.nextstep.nextsiheung.domain.user.service;

import com.nextstep.nextsiheung.domain.user.repository.UserVisitRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 접속 기록(user_visit). 요청마다 DB에 쓰지 않도록, 오늘 이미 기록한 회원은 메모리에서 걸러낸다
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVisitService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final UserVisitRepository userVisitRepository;
    private final Map<Long, LocalDate> recordedToday = new ConcurrentHashMap<>();

    @Transactional
    public void recordToday(Long userId) {
        LocalDate today = LocalDate.now(SEOUL);
        if (today.equals(recordedToday.get(userId))) {
            return;
        }
        userVisitRepository.insertIgnore(userId, today);
        recordedToday.put(userId, today);
    }

    public void forget(Long userId) {
        recordedToday.remove(userId);
    }
}
