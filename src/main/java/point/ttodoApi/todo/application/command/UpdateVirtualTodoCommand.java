package point.ttodoApi.todo.application.command;

import java.time.*;
import java.util.*;

public record UpdateVirtualTodoCommand(
        String virtualTodoId,
        UUID userId,
        String title,
        String description,
        Boolean complete,
        Integer priorityId,
        UUID categoryId,
        LocalDate date,
        LocalTime time,
        Set<String> tags
) {
}
