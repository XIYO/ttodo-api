package point.ttodoApi.category.application.command;

import java.util.UUID;

public record UpdateCategoryCommand(
        UUID memberId,
        UUID categoryId,
        String name,
        String color,
        String description
) {
}
