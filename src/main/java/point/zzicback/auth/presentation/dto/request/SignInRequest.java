package point.zzicback.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "사용자 사인-인 요청 DTO")
public record SignInRequest(
        @Schema(description = "사용자 이메일", example = "anon@zzic.com") @NotBlank(message = "이메일은 필수 입력 항목입니다.") @Email(message = "{email.valid}") String email,
        @Schema(description = "사용자 비밀번호", example = "") String password) {
}
