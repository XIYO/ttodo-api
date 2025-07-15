package point.ttodoApi.member.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.common.specification.SpecificationBuilder;
import point.ttodoApi.common.specification.SortValidator;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.domain.Role;
import point.ttodoApi.member.dto.request.MemberSearchRequest;
import point.ttodoApi.member.infrastructure.MemberRepository;
import point.ttodoApi.member.infrastructure.MemberSpecification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 동적 쿼리를 사용한 Member 검색 서비스 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSearchService {
    
    private final MemberRepository memberRepository;
    private final MemberSpecification memberSpecification;
    private final SortValidator sortValidator;
    
    /**
     * 멤버 검색 - 다양한 조건으로 검색
     */
    public Page<Member> searchMembers(MemberSearchRequest request, Pageable pageable) {
        // 정렬 필드 검증
        sortValidator.validateSort(pageable.getSort(), memberSpecification);
        
        // SpecificationBuilder를 사용한 동적 쿼리 구성
        SpecificationBuilder<Member> builder = new SpecificationBuilder<>(memberSpecification);
        
        Specification<Member> spec = builder
                // 기본 조건 - 활성 사용자만
                .with("active", true)
                
                // 선택적 조건들
                .withLike("email", request.getEmailKeyword())
                .withLike("nickname", request.getNicknameKeyword())
                .with("role", request.getRole())
                .withDateRange("lastLoginAt", request.getLastLoginFrom(), request.getLastLoginTo())
                
                // 복잡한 조건 예제 - 최근 활동 사용자
                .withIf(request.isRecentlyActiveOnly(), builder2 -> 
                    builder2.withBetween("lastLoginAt", 
                            LocalDateTime.now().minusDays(30), 
                            LocalDateTime.now()))
                
                .build();
        
        return memberRepository.findAll(spec, pageable);
    }
    
    /**
     * 활성 관리자 목록 조회
     */
    public List<Member> getActiveAdmins() {
        SpecificationBuilder<Member> builder = new SpecificationBuilder<>(memberSpecification);
        
        Specification<Member> spec = builder
                .with("active", true)
                .with("role", Role.ADMIN)
                .build();
        
        return memberRepository.findAll(spec);
    }
    
    /**
     * 오랜 기간 로그인하지 않은 사용자 조회
     */
    public Page<Member> getInactiveMembers(int days, Pageable pageable) {
        SpecificationBuilder<Member> builder = new SpecificationBuilder<>(memberSpecification);
        
        Specification<Member> spec = builder
                .with("active", true)
                .lessThan("lastLoginAt", LocalDateTime.now().minusDays(days))
                .build();
        
        return memberRepository.findAll(spec, pageable);
    }
    
}