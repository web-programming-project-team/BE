package com.nextstep.nextsiheung.domain.user.entity;

import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD users. 주민등록번호·소득 정보는 두지 않는다
@Getter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "UQ_USERS_EMAIL", columnNames = "email"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    // BCrypt 해시
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(columnDefinition = "SMALLINT")
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean onboardingCompleted;

    @Column(nullable = false)
    private boolean agreeTerms;

    @Column(nullable = false)
    private boolean agreePrivacy;

    private LocalDateTime lastLoginAt;

    // ERD user_life_stage. 온보딩·상황 변경에서 받는 현재 상황 (복수)
    @ElementCollection
    @CollectionTable(name = "user_life_stage", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "life_stage", nullable = false, length = 30)
    private Set<LifeStage> lifeStages = new LinkedHashSet<>();

    // ERD user_interest. 관심분야 (복수)
    @ElementCollection
    @CollectionTable(name = "user_interest", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_field", nullable = false, length = 30)
    private Set<InterestField> interests = new LinkedHashSet<>();

    @Builder
    private User(String email, String password, String nickname, Integer birthYear, Role role,
                 boolean agreeTerms, boolean agreePrivacy) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birthYear = birthYear;
        this.role = role == null ? Role.USER : role;
        this.agreeTerms = agreeTerms;
        this.agreePrivacy = agreePrivacy;
    }

    public void recordLogin(LocalDateTime now) {
        this.lastLoginAt = now;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void changeStatus(Collection<LifeStage> lifeStages, Collection<InterestField> interests) {
        this.lifeStages.clear();
        this.lifeStages.addAll(lifeStages);
        this.interests.clear();
        this.interests.addAll(interests);
    }
}
