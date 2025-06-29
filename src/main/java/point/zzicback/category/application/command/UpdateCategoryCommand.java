package point.zzicback.category.application.command;

import java.util.UUID;

public record UpdateCategoryCommand(
        UUID memberId,
        Long categoryId,
        String name,
        String color,
        String description
) {
}
