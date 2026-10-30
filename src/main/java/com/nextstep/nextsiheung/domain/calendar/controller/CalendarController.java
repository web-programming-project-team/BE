package com.nextstep.nextsiheung.domain.calendar.controller;

import com.nextstep.nextsiheung.domain.calendar.dto.CalendarEventCreateRequest;
import com.nextstep.nextsiheung.domain.calendar.dto.CalendarEventResponse;
import com.nextstep.nextsiheung.domain.calendar.dto.EventIdResponse;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calendar/events")
public class CalendarController implements CalendarApiDocs {

    @Override
    @GetMapping
    public ApiResponse<List<CalendarEventResponse>> monthly(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) List<EventType> types) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/upcoming")
    public ApiResponse<List<CalendarEventResponse>> upcoming(
            @RequestParam(defaultValue = "7") int days) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventIdResponse> create(@RequestBody CalendarEventCreateRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long eventId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
