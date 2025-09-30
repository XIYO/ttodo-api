package point.ttodoApi.shared.validation.threat;

import java.util.regex.Pattern;

public final class InputThreatPattern {

    public static final Pattern SQL_INJECTION = Pattern.compile(
        ".*(;|--|'|\"|\\*|xp_|sp_|<script|</script|<iframe|</iframe|javascript:|onclick=|onerror=|onload=).*",
        Pattern.CASE_INSENSITIVE
    );

    private InputThreatPattern() {
    }
}
