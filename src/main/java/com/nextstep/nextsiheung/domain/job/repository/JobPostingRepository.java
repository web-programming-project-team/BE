package com.nextstep.nextsiheung.domain.job.repository;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 맞춤 채용 조회의 조건: 관리자 마감·마감일 경과 공고 제외 + 희망 지역·학력·직무·고용형태 일치
// 조건 컬렉션은 비어 있으면 안 된다(서비스가 '조건 없음'을 enum 전체로 바꿔서 넘긴다)
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    String MATCHES = """
            j.closed = false and (j.deadline is null or j.deadline >= :today)
            and j.region in :regions and j.education in :educations
            and j.jobCategory in :categories and j.employmentType in :employmentTypes
            """;

    List<JobPosting> findByExternalIdIn(Collection<String> externalIds);

    // 마감순: 마감 임박 먼저, 상시채용(deadline null)은 맨 뒤
    @Query(value = "select j from JobPosting j where " + MATCHES + """
            order by case when j.deadline is null then 1 else 0 end, j.deadline asc, j.id desc
            """,
            countQuery = "select count(j) from JobPosting j where " + MATCHES)
    Page<JobPosting> findMatchesByDeadline(@Param("today") LocalDate today,
                                           @Param("regions") Collection<Region> regions,
                                           @Param("educations") Collection<Education> educations,
                                           @Param("categories") Collection<JobCategory> categories,
                                           @Param("employmentTypes") Collection<EmploymentType> employmentTypes,
                                           Pageable pageable);

    // 최신순: 최근 수집(등록)한 공고 먼저
    @Query(value = "select j from JobPosting j where " + MATCHES + " order by j.collectedAt desc, j.id desc",
            countQuery = "select count(j) from JobPosting j where " + MATCHES)
    Page<JobPosting> findMatchesByLatest(@Param("today") LocalDate today,
                                         @Param("regions") Collection<Region> regions,
                                         @Param("educations") Collection<Education> educations,
                                         @Param("categories") Collection<JobCategory> categories,
                                         @Param("employmentTypes") Collection<EmploymentType> employmentTypes,
                                         Pageable pageable);

    // 홈 newJobCount·NEW_JOB 알림: since 이후 수집된 조건 일치 공고 수
    @Query("select count(j) from JobPosting j where " + MATCHES + " and j.collectedAt >= :since")
    long countMatchesSince(@Param("today") LocalDate today,
                           @Param("regions") Collection<Region> regions,
                           @Param("educations") Collection<Education> educations,
                           @Param("categories") Collection<JobCategory> categories,
                           @Param("employmentTypes") Collection<EmploymentType> employmentTypes,
                           @Param("since") LocalDateTime since);

    // 관리자 공고 목록: 출처·마감 여부·키워드(회사명·제목)로 거른다. 조건이 null이면 거르지 않는다
    @Query("""
            select j from JobPosting j
            where (:source is null or j.source = :source)
              and (:keyword is null or j.company like :keyword or j.title like :keyword)
              and (:closed is null
                   or (:closed = true and (j.closed = true or j.deadline < :today))
                   or (:closed = false and j.closed = false and (j.deadline is null or j.deadline >= :today)))
            order by j.collectedAt desc, j.id desc
            """)
    Page<JobPosting> findForAdmin(@Param("source") JobSource source, @Param("closed") Boolean closed,
                                  @Param("keyword") String keyword, @Param("today") LocalDate today,
                                  Pageable pageable);

    // 공고-정책 매칭 대상: 접수 중인 공고의 [jobId, jobCategory]
    @Query("""
            select j.id, j.jobCategory from JobPosting j
            where j.closed = false and (j.deadline is null or j.deadline >= :today)
            """)
    List<Object[]> findOpenJobCategories(@Param("today") LocalDate today);
}
