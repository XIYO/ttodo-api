package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.dto.query.MemberQuery;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findVerifiedMember(MemberQuery query) {
        return memberRepository
                .findById(query.memberId())
                .orElseThrow(() -> new EntityNotFoundException("Member", query.memberId()));
    }
}
