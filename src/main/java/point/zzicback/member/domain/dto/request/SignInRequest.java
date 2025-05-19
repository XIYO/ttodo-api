package point.zzicback.member.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(

        @NotBlank
        String email,

        @NotBlank
        String password
) {}