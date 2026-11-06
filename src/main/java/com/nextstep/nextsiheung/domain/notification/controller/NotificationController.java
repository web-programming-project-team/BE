package com.nextstep.nextsiheung.domain.notification.controller;

import com.nextstep.nextsiheung.domain.notification.dto.NotificationResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadAllResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadResponse;
import com.nextstep.nextsiheung.domain.notification.dto.UnreadCountResponse;
import com.nextstep.nextsiheung.domain.notification.service.NotificationService;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationApiDocs {

    private final NotificationService notificationService;

    @Override
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(notificationService.list(CurrentUser.id(), type, page, size));
    }

    @Override
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount() {
        return ApiResponse.ok(notificationService.unreadCount(CurrentUser.id()));
    }

    @Override
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<ReadResponse> read(@PathVariable Long notificationId) {
        return ApiResponse.ok(notificationService.read(CurrentUser.id(), notificationId));
    }

    @Override
    @PatchMapping("/read-all")
    public ApiResponse<ReadAllResponse> readAll() {
        return ApiResponse.ok(notificationService.readAll(CurrentUser.id()));
    }
}
