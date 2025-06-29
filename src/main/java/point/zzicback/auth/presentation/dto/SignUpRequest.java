package point.zzicback.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.zzicback.common.validation.fieldcompare.*;
import point.zzicback.member.domain.validation.UniqueEmail;

@Schema(description = "사용자 사인-업에 필요한 데이터 DTO")
@FieldComparison(message = "패스워드와 확인 패스워드가 일치하지 않습니다.")
public record SignUpRequest(
        @NotBlank(message = "{email.required}") 
        @Schema(description = "사용자 이메일", example = "user@example.com") 
        @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "{email.valid}") 
        @UniqueEmail(message = "이미 등록된 이메일입니다.") 
        String email,
        
        @NotBlank(message = "{password.required}") 
        @Schema(description = "사용자 비밀번호", example = "Strong@123") 
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$", message = "비밀번호는 최소 8자, 최대 16자, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.") 
        @CompareTarget 
        String password,
        
        @Schema(description = "비밀번호 확인", example = "Strong@123", requiredMode = Schema.RequiredMode.REQUIRED) 
        @NotBlank(message = "패스워드 확인을 입력해주세요.") 
        @CompareResult 
        String confirmPassword,
        
        @NotBlank(message = "{nickname.required}") 
        @Schema(description = "사용자 이름", example = "홍길동") 
        String nickname,
        
        @Schema(description = "소개글", example = "안녕하세요! 새로운 회원입니다.")
        String introduction,
        @Schema(description = "사용자 타임존", example = "Asia/Seoul")
        String timeZone,
        @Schema(description = "사용자 로케일", example = "ko_KR")
        String locale) {
}
