package point.ttodoApi.experience.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import point.ttodoApi.experience.application.result.UserLevelResult;
import point.ttodoApi.experience.domain.UserExperience;
import point.ttodoApi.level.domain.Level;

/**
 * Experience Application Mapper
 * TTODO 아키텍처 패턴: Application Layer 매퍼
 * Domain ↔ Application DTO 변환
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface ExperienceApplicationMapper {

    /**
     * Experience와 Level 정보를 UserLevelResult로 변환
     */
    default UserLevelResult toUserLevelResult(UserExperience experience, Level currentLevel, Level nextLevel) {
        // 현재 레벨의 시작 경험치 (이전 레벨들의 경험치 합계)
        int currentLevelMinExp = calculateMinExp(currentLevel.getLevel());
        
        // 현재 레벨 내에서의 경험치 진행도 계산
        int currentLevelExp = experience.getExperience() - currentLevelMinExp;
        int currentLevelTotal = currentLevel.getRequiredExp();
        
        // 다음 레벨까지 필요한 경험치 계산
        int expToNextLevel = nextLevel != null 
            ? (currentLevelMinExp + currentLevel.getRequiredExp()) - experience.getExperience()
            : 0;
        
        return new UserLevelResult(
            currentLevel.getLevel(),
            currentLevel.getName(),
            experience.getExperience(),
            currentLevelMinExp,
            expToNextLevel,
            currentLevelExp,
            currentLevelTotal
        );
    }
    
    /**
     * 레벨별 시작 경험치 계산 (임시 구현)
     * 실제로는 레벨 서비스에서 계산해야 함
     */
    default int calculateMinExp(int level) {
        // 레벨 1은 0부터 시작
        if (level <= 1) return 0;
        
        // 간단한 계산: 이전 레벨들의 필요 경험치를 모두 더한 값
        // 실제 구현에서는 레벨 서비스를 통해 정확한 값을 가져와야 함
        return (level - 1) * 100; // 임시 계산
    }
    
    /**
     * Experience 엔티티의 기본 정보를 UserLevelResult로 변환 (레벨 정보 없음)
     */
    @Mapping(target = "levelName", constant = "Unknown")
    @Mapping(target = "currentLevelMinExp", constant = "0")
    @Mapping(target = "expToNextLevel", constant = "0")  
    @Mapping(target = "currentLevelProgress", constant = "0")
    @Mapping(target = "currentLevelTotal", constant = "0")
    @Mapping(source = "experience", target = "currentExp")
    @Mapping(expression = "java(1)", target = "currentLevel") // 기본 레벨 1
    UserLevelResult toBasicResult(UserExperience experience);
}