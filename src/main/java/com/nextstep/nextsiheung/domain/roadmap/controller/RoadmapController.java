package com.nextstep.nextsiheung.domain.roadmap.controller;

import com.nextstep.nextsiheung.domain.roadmap.dto.HomeSummaryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapHistoryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RoadmapController implements RoadmapApiDocs {

    @Override
    @GetMapping("/home/summary")
    public ApiResponse<HomeSummaryResponse> homeSummary() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/roadmaps/me")
    public ApiResponse<RoadmapResponse> myRoadmap() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/roadmap-items/{itemId}/status")
    public ApiResponse<StatusChangeResponse> changeStatus(@PathVariable Long itemId,
                                                          @RequestBody StatusChangeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/roadmaps/me/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoadmapItemAddResponse> addItem(@RequestBody RoadmapItemAddRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @DeleteMapping("/roadmap-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/roadmaps/me/history")
    public ApiResponse<List<RoadmapHistoryResponse>> history() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/roadmaps/{roadmapId}")
    public ApiResponse<RoadmapResponse> roadmapDetail(@PathVariable Long roadmapId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
