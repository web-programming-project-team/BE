package com.nextstep.nextsiheung.domain.notification.controller;

import com.nextstep.nextsiheung.domain.notification.dto.NotificationResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadAllResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadResponse;
import com.nextstep.nextsiheung.domain.notification.dto.UnreadCountResponse;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationApiDocs {

    @Override
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<ReadResponse> read(@PathVariable Long notificationId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/read-all")
    public ApiResponse<ReadAllResponse> readAll() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
