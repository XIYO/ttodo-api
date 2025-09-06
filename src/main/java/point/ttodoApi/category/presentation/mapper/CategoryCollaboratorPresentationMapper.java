package point.ttodoApi.category.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.presentation.dto.CollaboratorResponse;
import point.ttodoApi.shared.config.MapStructConfig;

/**
 * Category Collaborator Presentation Mapper
 * Domain -> Presentation(Response) 변환 전용 매퍼
 */
@Mapper(config = MapStructConfig.class)
public interface CategoryCollaboratorPresentationMapper {

  @Mappings({
          @Mapping(source = "category.id", target = "categoryId"),
          @Mapping(source = "category.name", target = "categoryName"),
          @Mapping(source = "member.id", target = "memberId"),
          @Mapping(source = "member.nickname", target = "memberNickname"),
          @Mapping(source = "member.email", target = "memberEmail")
  })
  CollaboratorResponse toResponse(CategoryCollaborator collaborator);
}

