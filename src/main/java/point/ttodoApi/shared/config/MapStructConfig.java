package point.ttodoApi.shared.config.shared;

import org.mapstruct.*;

/**
 * MapStruct 공통 설정
 * 모든 매퍼에서 상속받아 사용하는 공통 설정 클래스
 */
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
}