package com.nextstep.nextsiheung.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContactInfoDetectorTest {

    @Test
    void 전화번호_이메일_카톡_아이디를_찾는다() {
        assertThat(ContactInfoDetector.containsContact("연락은 010-1234-5678")).isTrue();
        assertThat(ContactInfoDetector.containsContact("01012345678로")).isTrue();
        assertThat(ContactInfoDetector.containsContact("031 123 4567")).isTrue();
        assertThat(ContactInfoDetector.containsContact("me@test.kr로 메일")).isTrue();
        assertThat(ContactInfoDetector.containsContact("카톡 아이디 nextstep")).isTrue();
        assertThat(ContactInfoDetector.containsContact(null, "", "오픈채팅 ID 알려드려요")).isTrue();
    }

    @Test
    void 날짜_숫자_일반_문장은_통과한다() {
        assertThat(ContactInfoDetector.containsContact("2026-10-28까지 8주 과정, 수강생 120명")).isFalse();
        assertThat(ContactInfoDetector.containsContact("카톡으로 공지가 와서 편했어요")).isFalse();
        assertThat(ContactInfoDetector.containsContact(null, " ")).isFalse();
    }
}
