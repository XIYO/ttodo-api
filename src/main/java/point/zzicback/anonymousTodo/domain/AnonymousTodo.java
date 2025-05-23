package point.zzicback.anonymousTodo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@RequiredArgsConstructor
@Setter
public class AnonymousTodo {

    @Id
    @GeneratedValue
    private Long id;

    private String guestId;

    private String content;

    private boolean done;
}
