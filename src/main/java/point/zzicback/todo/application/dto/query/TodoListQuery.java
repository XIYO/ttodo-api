package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TodoListQuery(
        UUID memberId,
        List<Integer> statusIds,
        List<Long> categoryIds,
        List<Integer> priorityIds,
        List<String> tags,
        String keyword,
        List<Integer> hideStatusIds,
        Instant startDate,
        Instant endDate,
        Pageable pageable
) {}
