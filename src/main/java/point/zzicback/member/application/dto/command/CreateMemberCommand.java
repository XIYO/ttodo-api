package point.zzicback.member.application.dto.command;

public record CreateMemberCommand(
        String email,
        String password,
        String nickname,
        String introduction,
        String timeZone,
        String locale) {

    public CreateMemberCommand(String email, String password, String nickname, String introduction) {
        this(email, password, nickname, introduction, "Asia/Seoul", "ko_KR");
    }
}
