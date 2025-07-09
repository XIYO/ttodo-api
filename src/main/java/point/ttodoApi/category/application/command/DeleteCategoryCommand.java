package point.ttodoApi.category.application.command;

import java.util.UUID;

public record DeleteCategoryCommand(
        UUID memberId,
        UUID categoryId
) {
}
