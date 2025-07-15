package point.ttodoApi.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import point.ttodoApi.member.domain.Member;

import java.util.*;

public interface MemberRepository extends JpaRepository<Member, UUID>, JpaSpecificationExecutor<Member> {
  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
}
