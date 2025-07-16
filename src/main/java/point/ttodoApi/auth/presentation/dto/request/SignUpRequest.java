package point.ttodoApi.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.ttodoApi.common.validation.fieldcompare.*;
import point.ttodoApi.member.domain.validation.UniqueEmail;
import point.ttodoApi.common.validation.annotations.NoSqlInjection;
import point.ttodoApi.common.validation.annotations.SecurePassword;
import point.ttodoApi.common.validation.annotations.ValidUsername;
import point.ttodoApi.common.validation.annotations.SanitizeHtml;
import point.ttodoApi.common.validation.annotations.ValidEmail;

@Schema(description = "사용자 사인-업에 필요한 데이터 DTO")
@FieldComparison(message = "패스워드와 확인 패스워드가 일치하지 않습니다.")
public record SignUpRequest(
        @NotBlank(message = "{email.required}") 
        @Schema(description = "사용자 이메일", example = "user@example.com") 
        @ValidEmail(allowDisposable = false)
        @UniqueEmail(message = "이미 등록된 이메일입니다.") 
        String email,
        
        @NotBlank(message = "{password.required}") 
        @Schema(description = "사용자 비밀번호", example = "Strong@123") 
        @SecurePassword
        @Size(min = 4, max = 100, message = "비밀번호는 4자 이상 100자 이하여야 합니다.")
        @CompareTarget 
        String password,
        
        @Schema(description = "비밀번호 확인", example = "Strong@123", requiredMode = Schema.RequiredMode.REQUIRED) 
        @NotBlank(message = "패스워드 확인을 입력해주세요.") 
        @CompareResult 
        String confirmPassword,
        
        @NotBlank(message = "{nickname.required}") 
        @Schema(description = "사용자 이름", example = "홍길동") 
        @ValidUsername
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        @NoSqlInjection
        String nickname,
        
        @Schema(description = "소개글", example = "안녕하세요! 새로운 회원입니다.")
        @SanitizeHtml(mode = SanitizeHtml.SanitizeMode.STRICT)
        String introduction,
        @Schema(description = "사용자 타임존", example = "Asia/Seoul")
        String timeZone,
        @Schema(description = "사용자 로케일", example = "ko_KR")
        String locale) {
}
