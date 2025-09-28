package point.ttodoApi.profile.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import point.ttodoApi.profile.application.command.UpdateProfileCommand;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;

/**
 * Profile Application Mapper
 * TTODO 아키텍처 패턴: Application Layer 매퍼
 * Domain ↔ Application DTO 변환
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface ProfileApplicationMapper {

    /**
     * UpdateProfileCommand → Profile 업데이트 매핑
     * 
     * Note: Profile은 업데이트 메서드를 통해 변경되므로 직접 매핑보다는 
     * 서비스에서 개별 필드 업데이트 방식을 권장
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "nickname", ignore = true) // Profile constructor에서 설정
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "profileImageType", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "timeZone", expression = "java(command.timeZone() != null ? command.timeZone().toString() : null)")
    @Mapping(target = "locale", expression = "java(command.locale() != null ? command.locale().toString() : null)")
    @Mapping(target = "theme", expression = "java(mapTheme(command.theme()))")
    Profile toEntity(UpdateProfileCommand command);
    
    /**
     * Profile 엔티티의 필드별 업데이트 여부 확인
     */
    default boolean hasIntroduction(UpdateProfileCommand command) {
        return command.introduction() != null;
    }
    
    default boolean hasTimeZone(UpdateProfileCommand command) {
        return command.timeZone() != null;
    }
    
    default boolean hasLocale(UpdateProfileCommand command) {
        return command.locale() != null;
    }
    
    default boolean hasTheme(UpdateProfileCommand command) {
        return command.theme() != null && !command.theme().trim().isEmpty();
    }
    
    /**
     * String 테마명을 Theme 열거형으로 변환
     */
    default Theme mapTheme(String theme) {
        if (theme == null || theme.trim().isEmpty()) {
            return null;
        }
        try {
            return Theme.valueOf(theme.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Theme.PINKY; // 기본 테마로 설정
        }
    }
}
