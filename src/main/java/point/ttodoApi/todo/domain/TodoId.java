package point.ttodoApi.todo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class TodoId implements Serializable {

  @Column(name = "original_todo_id")
  private Long id;

  @Column(name = "days_difference")
  private Long seq;

  public static TodoId fromVirtualId(String virtualId) {
    String[] parts = virtualId.split(":");
    return new TodoId(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
  }

  public Long getDaysDifference() {
    return seq;
  }

  public String getVirtualId() {
    return id + ":" + seq;
  }

  @Override
  public String toString() {
    return getVirtualId();
  }
}
