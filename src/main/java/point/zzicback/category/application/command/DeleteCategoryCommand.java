package point.zzicback.category.application.command;

import java.util.UUID;

public record DeleteCategoryCommand(
        UUID memberId,
        Long categoryId
) {
}
