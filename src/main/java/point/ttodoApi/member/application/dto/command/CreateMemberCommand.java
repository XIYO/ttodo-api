package point.ttodoApi.member.application.dto.command;

public record CreateMemberCommand(
        String email,
        String password,
        String nickname,
        String introduction) {
}
