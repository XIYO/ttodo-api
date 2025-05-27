package point.zzicback.member.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.common.jwt.RefreshTokenService;
import point.zzicback.common.security.etc.MemberPrincipal;
import point.zzicback.common.utill.CookieUtil;
import point.zzicback.common.utill.JwtUtil;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.AuthenticatedMember;
import point.zzicback.member.domain.dto.request.SignInRequest;
import point.zzicback.member.domain.dto.request.SignUpRequest;
import point.zzicback.member.domain.dto.response.MemberMeResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up")
    public void signUpJson(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request.toCommand());
    }

    @PostMapping("/sign-in")
    public void signInJson(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        AuthenticatedMember authenticatedMember = memberService.signIn(request.toCommand());
        String jwtToken = jwtUtil.generateAccessToken(authenticatedMember.id(), authenticatedMember.email(), authenticatedMember.nickname());
        Cookie jwtCookie = cookieUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);

        String refreshToken = jwtUtil.generateRefreshToken(authenticatedMember.id(), authenticatedMember.email(), authenticatedMember.nickname());

        refreshTokenService.save(UUID.fromString(authenticatedMember.id()), refreshToken, jwtToken);
    }

    @PostMapping("/sign-out")
    public void signOut(HttpServletResponse response) {
        Cookie expiredCookie = cookieUtil.createJwtCookie("");
        cookieUtil.zeroAge(expiredCookie);
        response.addCookie(expiredCookie);
    }

    @GetMapping("/me")
    public MemberMeResponse getMe(@AuthenticationPrincipal MemberPrincipal principal) {
        UUID memberId = principal.id();
        return memberService.getMemberMe(memberId);
    }
}
