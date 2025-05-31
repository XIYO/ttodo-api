package point.zzicback.member.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import point.zzicback.member.domain.Member;

import java.util.*;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);
}
