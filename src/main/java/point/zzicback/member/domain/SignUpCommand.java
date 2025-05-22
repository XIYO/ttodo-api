package point.zzicback.member.domain;

public record SignUpCommand(
        String email,
        String password,
        String nickName
) {}