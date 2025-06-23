package point.zzicback.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.member.domain.Member;

import java.util.*;

public interface MemberRepository extends JpaRepository<Member, UUID> {
  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
}
