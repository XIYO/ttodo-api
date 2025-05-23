package point.zzicback.anonymous.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Setter;

import java.util.UUID;

@Entity
public class Anonymous {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(unique = true)
    @Setter
    private String email;

    @Setter
    private String nickname;

    @Setter
    private String password;
}
