package point.zzicback.member.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import point.zzicback.member.application.dto.query.MemberQuery;
import point.zzicback.member.application.dto.response.MemberMeResponse;
import point.zzicback.member.application.mapper.MemberApplicationMapper;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberApplicationMapper memberApplicationMapper;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberMe_success() {
        UUID memberId = UUID.randomUUID();
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("user@example.com");
        member.setNickname("테스트유저");

        MemberMeResponse expectedResponse = new MemberMeResponse("user@example.com", "테스트유저");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberApplicationMapper.toResponse(member)).thenReturn(expectedResponse);

        MemberMeResponse result = memberService.getMemberMe(memberId);

        assertEquals("user@example.com", result.email());
        assertEquals("테스트유저", result.nickname());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 사용자 없음")
    void getMemberMe_fail_userNotFound() {
        UUID memberId = UUID.randomUUID();

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> memberService.getMemberMe(memberId));
    }

    @Test
    @DisplayName("쿼리로 회원 정보 조회 성공")
    void getMemberMe_withQuery_success() {
        UUID memberId = UUID.randomUUID();
        MemberQuery query = MemberQuery.of(memberId);
        
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("user@example.com");
        member.setNickname("테스트유저");

        MemberMeResponse expectedResponse = new MemberMeResponse("user@example.com", "테스트유저");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberApplicationMapper.toResponse(member)).thenReturn(expectedResponse);

        MemberMeResponse result = memberService.getMemberMe(query);

        assertEquals("user@example.com", result.email());
        assertEquals("테스트유저", result.nickname());
    }
}
