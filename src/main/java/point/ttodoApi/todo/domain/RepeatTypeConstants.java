package point.ttodoApi.todo.domain;

public final class RepeatTypeConstants {
    public static final int NONE = 0;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;
    public static final int MONTHLY = 3;
    public static final int YEARLY = 4;
    
    private RepeatTypeConstants() {
    }
    
    public static String getRepeatTypeName(Integer repeatType) {
        if (repeatType == null) return "없음";
        return switch (repeatType) {
            case NONE -> "없음";
            case DAILY -> "매일";
            case WEEKLY -> "매주";
            case MONTHLY -> "매월";
            case YEARLY -> "매년";
            default -> "알 수 없음";
        };
    }
    
    public static boolean isValidRepeatType(Integer repeatType) {
        return repeatType != null && repeatType >= NONE && repeatType <= YEARLY;
    }
}
