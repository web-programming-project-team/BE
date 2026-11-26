package com.nextstep.nextsiheung.domain.roadmap.controller;

import com.nextstep.nextsiheung.domain.roadmap.dto.HomeSummaryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapHistoryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeResponse;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RoadmapController implements RoadmapApiDocs {

    private final RoadmapService roadmapService;

    @Override
    @GetMapping("/home/summary")
    public ApiResponse<HomeSummaryResponse> homeSummary() {
        return ApiResponse.ok(roadmapService.homeSummary(CurrentUser.id()));
    }

    @Override
    @GetMapping("/roadmaps/me")
    public ApiResponse<RoadmapResponse> myRoadmap() {
        return ApiResponse.ok(roadmapService.myRoadmap(CurrentUser.id()));
    }

    @Override
    @PatchMapping("/roadmap-items/{itemId}/status")
    public ApiResponse<StatusChangeResponse> changeStatus(@PathVariable Long itemId,
                                                          @RequestBody StatusChangeRequest request) {
        return ApiResponse.ok(roadmapService.changeStatus(CurrentUser.id(), itemId, request.status()));
    }

    @Override
    @PostMapping("/roadmaps/me/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoadmapItemAddResponse> addItem(@RequestBody RoadmapItemAddRequest request) {
        return ApiResponse.ok(roadmapService.addItem(CurrentUser.id(), request.policyId(), request.status()));
    }

    @Override
    @DeleteMapping("/roadmap-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long itemId) {
        roadmapService.deleteItem(CurrentUser.id(), itemId);
    }

    @Override
    @GetMapping("/roadmaps/me/history")
    public ApiResponse<List<RoadmapHistoryResponse>> history() {
        return ApiResponse.ok(roadmapService.history(CurrentUser.id()));
    }

    @Override
    @GetMapping("/roadmaps/{roadmapId}")
    public ApiResponse<RoadmapResponse> roadmapDetail(@PathVariable Long roadmapId) {
        return ApiResponse.ok(roadmapService.detail(CurrentUser.id(), roadmapId));
    }
}
