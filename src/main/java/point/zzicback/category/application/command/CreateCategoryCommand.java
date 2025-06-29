package point.zzicback.category.application.command;

import java.util.UUID;

public record CreateCategoryCommand(
        UUID memberId,
        String name,
        String color,
        String description
) {
}
