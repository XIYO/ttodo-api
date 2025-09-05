package point.ttodoApi.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import point.ttodoApi.shared.validation.annotations.ValidEmail;

@Schema(description = "사용자 사인-인 요청 DTO")
public record SignInRequest(
        @NotBlank(message = "이메일은 필수 입력 항목입니다.") 
        @Schema(description = "사용자 이메일", example = "anon@ttodo.dev") 
        @ValidEmail(allowDisposable = true) // Login allows disposable emails
        String email,
        
        @Schema(description = "사용자 비밀번호", example = "") 
        String password) {
}
