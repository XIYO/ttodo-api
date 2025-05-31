package point.zzicback.auth.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.auth.application.dto.command.SignInCommand;
import point.zzicback.auth.application.dto.command.SignUpCommand;
import point.zzicback.auth.presentation.dto.SignInRequest;
import point.zzicback.auth.presentation.dto.SignUpRequest;

@Mapper(componentModel = "spring")
public interface AuthPresentationMapper {
SignUpCommand toCommand(SignUpRequest request);

SignInCommand toCommand(SignInRequest request);
}
