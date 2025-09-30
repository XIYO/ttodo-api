package point.ttodoApi.shared.config;

import org.mapstruct.*;

@MapperConfig(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.WARN,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = false),
        // 컬렉션 매핑 전략
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface MapStructConfig {
    
    /**
     * Instant를 LocalDateTime으로 변환
     * BaseEntity의 createdAt/updatedAt (Instant)을 LocalDateTime으로 매핑할 때 사용
     */
    default java.time.LocalDateTime instantToLocalDateTime(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
    }
    
    /**
     * LocalDateTime을 Instant로 변환
     * LocalDateTime을 BaseEntity의 Instant 필드로 매핑할 때 사용
     */
    default java.time.Instant localDateTimeToInstant(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
    }
}