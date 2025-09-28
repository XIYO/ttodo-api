package point.ttodoApi.todo.application.query;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

public record TodoSearchQuery(
        UUID userId,
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
    Objects.requireNonNull(userId, "userId는 필수입니다");
    Objects.requireNonNull(pageable, "pageable은 필수입니다");
  }
}
