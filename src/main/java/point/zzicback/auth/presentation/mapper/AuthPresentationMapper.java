package point.zzicback.auth.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.auth.application.dto.command.*;
import point.zzicback.auth.presentation.dto.*;

@Mapper(componentModel = "spring")
public interface AuthPresentationMapper {
SignUpCommand toCommand(SignUpRequest request);

SignInCommand toCommand(SignInRequest request);
}
