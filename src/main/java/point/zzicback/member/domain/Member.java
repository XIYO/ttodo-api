package point.zzicback.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
public class Member implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @Setter
    private UUID id;

    @Column(unique = true)
    @Setter
    private String email;

    @Setter
    private String nickname;

    @Setter
    private String password;
}
