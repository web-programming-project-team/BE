package com.nextstep.nextsiheung.domain.job.service;

import com.nextstep.nextsiheung.domain.user.entity.JobPreference;
import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.Region;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// 희망 채용 조건(job_preference)을 공고 조회 조건으로 바꾼 것
// - 비어 있는 복수 조건은 '조건 없음'이라 enum 전체로 채운다 (JPQL IN에 빈 목록을 넣지 않기 위해)
// - 학력: 공고 education은 '최소 학력'. 내 학력 이하를 요구하는 공고와 학력무관(ANY) 공고가 맞는다
public record JobFilter(List<Region> regions, List<Education> educations, List<JobCategory> categories,
                        List<EmploymentType> employmentTypes) {

    public static JobFilter of(JobPreference preference) {
        return new JobFilter(
                orAll(preference.getRegions(), Region.values()),
                educationsFor(preference.getEducation()),
                orAll(preference.getJobCategories(), JobCategory.values()),
                orAll(preference.getEmploymentTypes(), EmploymentType.values()));
    }

    static List<Education> educationsFor(Education mine) {
        if (mine == null || mine == Education.ANY) {
            return List.of(Education.values());
        }
        List<Education> allowed = new ArrayList<>();
        for (Education e : Education.values()) {
            if (e == Education.ANY || e.ordinal() <= mine.ordinal()) {
                allowed.add(e);
            }
        }
        return allowed;
    }

    private static <T> List<T> orAll(Collection<T> values, T[] all) {
        return values == null || values.isEmpty() ? Arrays.asList(all) : List.copyOf(values);
    }
}
