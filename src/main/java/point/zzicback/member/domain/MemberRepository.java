package point.zzicback.member.domain;

import java.util.*;

/**
 * Member 도메인을 위한 Repository 인터페이스
 * DDD 원칙에 따라 도메인 계층에 위치
 */
public interface MemberRepository {
  Optional<Member> findByEmail(String email);
  Optional<Member> findById(UUID id);
  boolean existsByEmail(String email);
  Member save(Member member);
  void deleteById(UUID id);
}
