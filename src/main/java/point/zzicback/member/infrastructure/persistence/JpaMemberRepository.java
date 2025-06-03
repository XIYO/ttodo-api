package point.zzicback.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.util.*;

/**
 * JPA를 이용한 MemberRepository 구현체
 * Infrastructure 계층에 위치하여 기술적 구현사항을 담당
 */
@Repository
public interface JpaMemberRepository extends JpaRepository<Member, UUID>, MemberRepository {
  // JpaRepository에서 기본 CRUD 메서드들을 상속받고
  // MemberRepository에서 도메인 특화 메서드들을 상속받음
  
  // 추가적인 JPA 특화 쿼리 메서드들이 필요하면 여기에 정의
}
