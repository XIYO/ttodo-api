package point.zzicback.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

public record TodoSearchQuery(
        UUID memberId,
        List<Integer> statusIds,
        List<Long> categoryIds,
        List<Integer> priorityIds,
        List<String> tags,
        String keyword,
        List<Integer> hideStatusIds,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
) {}
