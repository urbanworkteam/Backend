package kr.farmily.api.user.domain;

import jakarta.persistence.*;
import kr.farmily.api.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private String kakaoId;

    private String name;

    @Column(name = "phone_enc")
    private byte[] phoneEnc;

    private String email;

    private String handle;

    @Column(nullable = false)
    private String plan = "FREE";

    @Column(name = "onboarded_at")
    private OffsetDateTime onboardedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    private User(String kakaoId, String name, String email) {
        this.kakaoId = kakaoId;
        this.name = name;
        this.email = email;
        this.plan = "FREE";
    }

    public static User create(String kakaoId, String name, String email) {
        return new User(kakaoId, name, email);
    }

    public boolean isOnboarded() {
        return onboardedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void completeOnboarding(String handle) {
        this.handle = handle;
        this.onboardedAt = OffsetDateTime.now();
    }

    public void updateProfile(String name, String email) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
    }

    public void changeHandle(String newHandle) {
        this.handle = newHandle;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public void changePlan(String plan) {
        this.plan = plan;
    }
}
