package point.ttodoApi.todo.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

public record TodoSearchQuery(
        UUID memberId,
        Boolean complete,
        List<UUID> categoryIds,
        List<Integer> priorityIds,
        List<String> tags,
        String keyword,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate date,
        Pageable pageable
) {
    public TodoSearchQuery {
        Objects.requireNonNull(memberId, "memberId는 필수입니다");
        Objects.requireNonNull(pageable, "pageable은 필수입니다");
    }
}
