package com.nextstep.nextsiheung.domain.notification.service;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobBookmarkRepository;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.notification.entity.Notification;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 새 후기 알림(NEW_REVIEW). 게시글이 등록되면 글에 연결된 정책을 로드맵에 담았거나 공고를 관심 저장한 사용자에게 보낸다
// 작성자 본인은 빼고, 정책과 공고 둘 다에 해당하는 사용자에게는 1건만 보낸다(정책 이름 우선)
@Service
@RequiredArgsConstructor
public class NewReviewNotificationService {

    private static final int MAX_NAME_LENGTH = 60;

    private final RoadmapItemRepository roadmapItemRepository;
    private final JobBookmarkRepository jobBookmarkRepository;
    private final PolicyRepository policyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final NotificationService notificationService;

    // 받는 사용자 수를 돌려준다
    @Transactional
    public int send(Post post) {
        // 사용자 → 알림에 쓸 이름 (먼저 넣은 이름이 남는다)
        Map<Long, String> targets = new LinkedHashMap<>();
        if (post.getPolicyId() != null) {
            String name = policyRepository.findById(post.getPolicyId()).map(Policy::getTitle).orElse(null);
            if (name != null) {
                roadmapItemRepository.findCurrentUserIdsByPolicyId(post.getPolicyId())
                        .forEach(userId -> targets.putIfAbsent(userId, name));
            }
        }
        if (post.getJobId() != null) {
            String name = jobPostingRepository.findById(post.getJobId()).map(JobPosting::getCompany).orElse(null);
            if (name != null) {
                jobBookmarkRepository.findUserIdsByJobId(post.getJobId())
                        .forEach(userId -> targets.putIfAbsent(userId, name));
            }
        }
        targets.remove(post.getUserId());

        targets.forEach((userId, name) -> notificationService.create(userId, NotificationType.NEW_REVIEW,
                shorten(name) + "에 새 후기가 올라왔어요", post.getOneLiner(), Notification.TARGET_POST, post.getId()));
        return targets.size();
    }

    private static String shorten(String name) {
        return name.length() <= MAX_NAME_LENGTH ? name : name.substring(0, MAX_NAME_LENGTH) + "…";
    }
}
