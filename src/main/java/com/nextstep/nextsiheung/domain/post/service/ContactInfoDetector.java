package com.nextstep.nextsiheung.domain.post.service;

import java.util.regex.Pattern;

// 게시글에 연락처(전화번호·이메일·카카오톡 아이디)가 있는지 본다. 있으면 CONTACT_INFO_DETECTED
// 프론트(ReviewWriteModal)도 먼저 검사하지만 서버가 최종으로 막는다
final class ContactInfoDetector {

    private static final Pattern[] PATTERNS = {
            // 휴대폰: 010-1234-5678, 010 1234 5678, 01012345678
            Pattern.compile("01[016789][-.\\s]?\\d{3,4}[-.\\s]?\\d{4}"),
            // 일반 전화: 031-123-4567, 02.1234.5678 (구분자 필수)
            Pattern.compile("(?<!\\d)0\\d{1,2}[-.\\s]\\d{3,4}[-.\\s]\\d{4}(?!\\d)"),
            // 이메일
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+"),
            // 카카오톡 아이디 안내: "카톡 아이디", "오픈채팅 id"
            Pattern.compile("(카톡|카카오톡|오픈채팅)\\s*(아이디|id)", Pattern.CASE_INSENSITIVE),
    };

    private ContactInfoDetector() {
    }

    static boolean containsContact(String... texts) {
        for (String text : texts) {
            if (text == null || text.isBlank()) {
                continue;
            }
            for (Pattern pattern : PATTERNS) {
                if (pattern.matcher(text).find()) {
                    return true;
                }
            }
        }
        return false;
    }
}
