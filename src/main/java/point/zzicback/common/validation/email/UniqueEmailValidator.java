package point.zzicback.common.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import point.zzicback.member.application.MemberService;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final MemberService memberService;

    @Autowired
    public UniqueEmailValidator(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        if (email == null || email.isEmpty()) {
            return true;
        }
        return !memberService.isEmailTaken(email);
    }
}
