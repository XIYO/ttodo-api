package point.zzicback.member.domain.dto;

public record SignInCommand(
        String email,
        String password
) {}