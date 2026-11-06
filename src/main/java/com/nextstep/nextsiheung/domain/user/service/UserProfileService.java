package com.nextstep.nextsiheung.domain.user.service;

import com.nextstep.nextsiheung.domain.roadmap.entity.Roadmap;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapService;
import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeRequest;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileRequest;
import com.nextstep.nextsiheung.domain.user.dto.ProfileResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileSaveResponse;
import com.nextstep.nextsiheung.domain.user.entity.JobPreference;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.JobPreferenceRepository;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.global.common.EnumLists;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 상태·관심분야 등록(온보딩), 조회, 채용 조건 수정, 상황 변경
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final JobPreferenceRepository jobPreferenceRepository;
    private final RoadmapService roadmapService;

    // 처음이면 값을 저장하고 로드맵을 만든다. 이미 등록했다면 값만 고치고 로드맵은 그대로 둔다(상황 변경은 별도 API)
    @Transactional
    public ProfileSaveResponse saveProfile(Long userId, ProfileRequest request) {
        User user = findUser(userId);
        user.changeStatus(request.lifeStages(), request.interests());
        if (request.jobPreference() != null) {
            applyJobPreference(user, request.jobPreference());
        }
        Roadmap roadmap = roadmapService.findCurrent(userId).orElseGet(() -> roadmapService.createFor(user));
        user.completeOnboarding();
        return new ProfileSaveResponse(true, roadmap.getId(), roadmap.getTemplateName());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = findUser(userId);
        JobPreferenceDto preference = jobPreferenceRepository.findById(userId)
                .map(UserProfileService::toDto)
                .orElse(null);
        return new ProfileResponse(EnumLists.sorted(user.getLifeStages()), EnumLists.sorted(user.getInterests()),
                preference);
    }

    // 보낸 필드만 바꾼다
    @Transactional
    public JobPreferenceDto updateJobPreference(Long userId, JobPreferenceDto request) {
        return toDto(applyJobPreference(findUser(userId), request));
    }

    // 기존 로드맵을 활동 기록으로 보관하고 새 로드맵을 만든다. 첫 취업(FIRST_JOB)이 새로 생기면 합격 후기 작성을 제안
    @Transactional
    public LifeStageChangeResponse changeLifeStage(Long userId, LifeStageChangeRequest request) {
        User user = findUser(userId);
        Roadmap current = roadmapService.current(userId);
        boolean firstJobBefore = user.getLifeStages().contains(LifeStage.FIRST_JOB);
        user.changeStatus(request.lifeStages(), request.interests());
        Roadmap created = roadmapService.archiveAndRecreate(user, current);
        boolean suggestReview = !firstJobBefore && request.lifeStages().contains(LifeStage.FIRST_JOB);
        return new LifeStageChangeResponse(current.getId(), created.getId(), created.getTemplateName(),
                suggestReview);
    }

    private JobPreference applyJobPreference(User user, JobPreferenceDto dto) {
        Optional<JobPreference> found = jobPreferenceRepository.findById(user.getId());
        JobPreference preference = found.orElseGet(() -> new JobPreference(user));
        preference.apply(dto.regions(), dto.education(), dto.careerLevel(), dto.jobCategories(),
                dto.employmentTypes());
        return found.isPresent() ? preference : jobPreferenceRepository.save(preference);
    }

    private static JobPreferenceDto toDto(JobPreference p) {
        return new JobPreferenceDto(EnumLists.sorted(p.getRegions()), p.getEducation(), p.getCareerLevel(),
                EnumLists.sorted(p.getJobCategories()), EnumLists.sorted(p.getEmploymentTypes()));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}
