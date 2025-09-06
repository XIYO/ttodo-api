package point.ttodoApi.member.application.command;

public record CreateMemberCommand(
        String email,
        String password,
        String nickname,
        String introduction) {
}
