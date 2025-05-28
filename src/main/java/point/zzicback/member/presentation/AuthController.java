package point.zzicback.member.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.common.jwt.TokenService;
import point.zzicback.common.security.etc.MemberPrincipal;
import point.zzicback.common.security.resolver.MultiBearerTokenResolver;
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
    private final TokenService tokenService;
    private final MultiBearerTokenResolver tokenResolver;

    @PostMapping("/sign-up")
    public void signUpJson(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request.toCommand());
    }

    @PostMapping("/sign-in")
    public void signInJson(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        AuthenticatedMember authenticatedMember = memberService.signIn(request.toCommand());
        String deviceId = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(authenticatedMember.id(), authenticatedMember.email(), authenticatedMember.nickname());
        Cookie jwtCookie = cookieUtil.createJwtCookie(accessToken);
        response.addCookie(jwtCookie);

        String refreshToken = jwtUtil.generateRefreshToken(authenticatedMember.id(), deviceId);
        Cookie refreshCookie = cookieUtil.createRefreshCookie(refreshToken);
        response.addCookie(refreshCookie);

        tokenService.save(deviceId, refreshToken);
    }

    @PostMapping("/sign-out")
    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        // 1) 쿠키 즉시 만료
        Cookie expiredCookie = cookieUtil.createJwtCookie("");
        cookieUtil.zeroAge(expiredCookie);
        response.addCookie(expiredCookie);

        // 2) 요청에서 JWT 꺼내기
        String jwtToken = tokenResolver.resolve(request);
        if (jwtToken != null) {
            tokenService.deleteByToken(jwtToken);
        }
    }


    @GetMapping("/me")
    public MemberMeResponse getMe(@AuthenticationPrincipal MemberPrincipal principal) {
        UUID memberId = principal.id();
        return memberService.getMemberMe(memberId);
    }
}
