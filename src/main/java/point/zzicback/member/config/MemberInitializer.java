package point.zzicback.member.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

@Component
@RequiredArgsConstructor
public class MemberInitializer implements ApplicationRunner {
private final MemberRepository memberRepository;

@Override
public void run(ApplicationArguments args) {
  String anonymousEmail = "anon@zzic.com";
  if (memberRepository.findByEmail(anonymousEmail).isEmpty()) {
    Member anonymous = Member.builder().email(anonymousEmail).nickname("익명의 찍찍이").password("anonymous").build();
    memberRepository.save(anonymous);
  }
}
}
