package point.ttodoApi.todo.application.dto.command;

import java.time.*;
import java.util.*;

public record UpdateVirtualTodoCommand(
        String virtualTodoId,
        UUID memberId,
        String title,
        String description,
        Boolean complete,
        Integer priorityId,
        UUID categoryId,
        LocalDate date,
        LocalTime time,
        Set<String> tags
) {}
