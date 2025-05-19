package point.zzicback.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    @Setter
    private String email;

    @Setter
    private String nickName;

    @Setter
    private String password;
}
