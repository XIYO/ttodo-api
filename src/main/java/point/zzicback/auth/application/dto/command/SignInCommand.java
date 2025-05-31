package point.zzicback.auth.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignInCommand(
        @Schema(description = "이메일", example = "test@example.com") String email,
        @Schema(description = "비밀번호", example = "password1234") String password) {}
