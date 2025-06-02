package point.zzicback.common.validation.email;

import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import point.zzicback.member.persistance.MemberRepository;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
  private final MemberRepository memberRepository;

  @Autowired
  public UniqueEmailValidator(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null || email.isEmpty()) {
      return true;
    }
    return !memberRepository.existsByEmail(email);
  }
}
