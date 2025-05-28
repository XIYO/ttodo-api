package point.zzicback.member.domain.dto.command;

public record SignUpCommand(
        String email,
        String password,
        String nickname
) {}