package point.ttodoApi.todo.domain.recurrence;

import java.time.LocalDate;

public class EndCondition {
    private EndConditionType type;
    private LocalDate until;
    private Integer count;

    public EndCondition() {}

    public EndCondition(EndConditionType type, LocalDate until, Integer count) {
        this.type = type;
        this.until = until;
        this.count = count;
    }

    public EndConditionType getType() { return type; }
    public void setType(EndConditionType type) { this.type = type; }

    public LocalDate getUntil() { return until; }
    public void setUntil(LocalDate until) { this.until = until; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

