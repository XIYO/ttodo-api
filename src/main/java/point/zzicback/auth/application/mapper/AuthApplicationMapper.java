package point.zzicback.auth.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.auth.application.dto.command.SignUpCommand;
import point.zzicback.member.domain.Member;

@Mapper(componentModel = "spring")
public interface AuthApplicationMapper {
@Mapping(target = "id", ignore = true)
Member toEntity(SignUpCommand command);
}
