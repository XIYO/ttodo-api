package point.ttodoApi.profile.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import point.ttodoApi.profile.domain.validation.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;

import static point.ttodoApi.profile.domain.StatisticsConstants.*;

@Entity
@Table(name = "statistics")
@Getter
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"owner"}) // 순환참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class Statistics extends BaseEntity {

  @EqualsAndHashCode.Include
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false, unique = true)
  @NotNull(message = OWNER_REQUIRED_MESSAGE)
  User owner;

  @Column
  @ValidSucceededTodosCount
  @Builder.Default
  Integer succeededTodosCount = DEFAULT_COUNT;

  @Column
  @ValidSucceededChallengesCount
  @Builder.Default
  Integer succeededChallengesCount = DEFAULT_COUNT;

  @Column
  @ValidFocusTime
  @Builder.Default
  Long totalFocusTime = DEFAULT_FOCUS_TIME; // in seconds

  @Column
  @ValidCurrentStreakDays
  @Builder.Default
  Integer currentStreakDays = DEFAULT_STREAK_DAYS;

  @Column
  @ValidLongestStreakDays
  @Builder.Default
  Integer longestStreakDays = DEFAULT_STREAK_DAYS;

  @Column
  @ValidCategoryCount
  @Builder.Default
  Integer categoryCount = DEFAULT_COUNT;




}
