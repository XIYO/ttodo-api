package point.zzicback.member.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;


import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberInitializer {

    private final MemberRepository memberRepository;

    @PostConstruct
    public void initializeAnonymousMember() {
        String anonymousEmail = "anonymous@shared.com";
        if (memberRepository.findByEmail(anonymousEmail).isEmpty()) {
            Member anonymous = new Member();
            anonymous.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            anonymous.setEmail(anonymousEmail);
            anonymous.setNickname("anonymous");
            memberRepository.save(anonymous);
        }
    }
}