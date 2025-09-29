package point.ttodoApi.category.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.presentation.dto.response.CollaboratorResponse;
import point.ttodoApi.shared.config.MapStructConfig;

/**
 * Category Collaborator Presentation Mapper
 * Domain -> Presentation(Response) 변환 전용 매퍼
 */
@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface CategoryCollaboratorPresentationMapper {

  @Mappings({
          @Mapping(source = "category.id", target = "categoryId"),
          @Mapping(source = "category.name", target = "categoryName"),
          @Mapping(source = "user.id", target = "userId"),
          @Mapping(target = "userNickname", ignore = true), // Profile에서 별도로 가져와야 함
          @Mapping(source = "user.email", target = "userEmail")
  })
  CollaboratorResponse toResponse(CategoryCollaborator collaborator);
}

