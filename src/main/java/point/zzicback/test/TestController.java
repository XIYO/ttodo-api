package point.zzicback.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.test.dto.TestSearchRequest;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Tag(name = "테스트", description = "ParameterObject 테스트용 API")
public class TestController {

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "테스트 목록 조회", description = "ParameterObject 테스트용 엔드포인트입니다.")
  public void getTest(@ParameterObject @Valid TestSearchRequest req) {
    
  }

  @GetMapping("/auth")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "인증 테스트 목록 조회", description = "AuthenticationPrincipal과 ParameterObject 테스트용 엔드포인트입니다.")
  public void getTestWithAuth(
          @AuthenticationPrincipal MemberPrincipal principal,
          @ParameterObject @Valid TestSearchRequest req) {
    // return new TestResponse(
    //     principal != null ? principal.id().hashCode() % 1000L : 999L,
    //     "인증 테스트 성공! 사용자 ID: " + (principal != null ? principal.id() : "없음"),
    //     req.dummy1(),
    //     req.dummy2(),
    //     req.dummyNumber(),
    //     req.dummyBoolean()
    // );
  }
}
