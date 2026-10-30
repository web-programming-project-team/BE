package com.nextstep.nextsiheung.domain.notification.controller;

import com.nextstep.nextsiheung.domain.notification.dto.NotificationResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadAllResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadResponse;
import com.nextstep.nextsiheung.domain.notification.dto.UnreadCountResponse;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "알림 센터", description = "서비스 안 알림. 이메일·푸시는 3순위라 이번 범위에 없다. 담당: 한동혁 · 11주차")
public interface NotificationApiDocs {

    @Operation(summary = "알림 목록 (2순위)", description = "헤더 종 아이콘의 드롭다운에 쓴다. link로 이동할 화면을 정한다.")
    ApiResponse<PageResponse<NotificationResponse>> list(
            @Parameter(description = "종류 필터") NotificationType type,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "안 읽은 알림 수 (2순위)", description = "헤더 빨간 점 숫자. 화면 이동 시마다 가볍게 호출한다.")
    ApiResponse<UnreadCountResponse> unreadCount();

    @Operation(summary = "알림 읽음 처리 (2순위)", description = "알림을 눌러 이동할 때 호출한다.")
    ApiResponse<ReadResponse> read(@Parameter(description = "알림 ID") Long notificationId);

    @Operation(summary = "모두 읽음 (2순위)")
    ApiResponse<ReadAllResponse> readAll();
}
