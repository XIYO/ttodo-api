package point.ttodoApi.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.MemberSearchService;
import point.ttodoApi.member.dto.request.MemberSearchRequest;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.member.application.dto.command.UpdateMemberCommand;
import point.ttodoApi.member.application.dto.result.MemberResult;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.presentation.dto.request.UpdateMemberRequest;
import point.ttodoApi.member.presentation.dto.response.MemberResponse;
import point.ttodoApi.member.presentation.mapper.MemberPresentationMapper;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.common.validation.ValidPageable;
import point.ttodoApi.common.validation.SortFieldsProvider;
import point.ttodoApi.common.dto.PageResponse;
import point.ttodoApi.profile.domain.*;

import java.io.IOException;
import java.util.UUID;

/**
 * 회원 관련 Presentation 레이어 API 컨트롤러
 */
@Tag(name = "회원(Member) 관리", description = "회원 정보 조회 및 관리를 위한 API를 제공합니다. 회원 목록 조회, 개인 정보 수정, 프로필 이미지 관리, 테마 설정 등의 기능을 포함합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberSearchService memberSearchService;
    private final ProfileService profileService;
    private final MemberPresentationMapper mapper;

    @Operation(
        summary = "회원 목록 조회/검색", 
        description = "시스템에 등록된 회원을 조회하거나 검색합니다. (관리자 전용)\n\n" +
                       "검색 파라미터:\n" +
                       "- emailKeyword: 이메일 키워드\n" +
                       "- nicknameKeyword: 닉네임 키워드\n" +
                       "- role: 권한 (USER, ADMIN)\n" +
                       "- active: 활성 상태\n" +
                       "- emailVerified: 이메일 인증 여부\n" +
                       "\n페이지네이션 파라미터:\n" +
                       "- page: 페이지 번호 (0부터 시작, 기본값: 0)\n" +
                       "- size: 페이지 크기 (기본값: 20)\n" +
                       "- sort: 정렬 기준 (id, nickname, createdAt 등)"
    )
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    @GetMapping
    @ValidPageable(sortFields = SortFieldsProvider.MEMBER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<MemberResponse>> getMembers(
            @Parameter(description = "검색 조건") @ModelAttribute MemberSearchRequest request,
            @Parameter(description = "페이징 및 정렬 정보")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        request.validate();
        
        // 검색 조건이 있으면 검색 서비스 사용, 없으면 기존 서비스 사용
        if (hasSearchCriteria(request)) {
            Page<Member> result = memberSearchService.searchMembers(request, pageable);
            return ResponseEntity.ok(PageResponse.of(result.map(mapper::toResponse)));
        } else {
            Page<MemberResult> result = memberService.getMembers(pageable);
            return ResponseEntity.ok(PageResponse.of(result.map(mapper::toResponse)));
        }
    }
    
    private boolean hasSearchCriteria(MemberSearchRequest request) {
        return request.hasSearchKeyword() || 
               request.hasRoleFilter() || 
               request.hasLoginDateFilter() ||
               request.getActive() != null ||
               request.getEmailVerified() != null ||
               request.isRecentlyActiveOnly();
    }

    @Operation(
        summary = "현재 로그인 사용자 정보 조회", 
        description = "JWT 액세스 토큰을 기반으로 현재 로그인한 사용자의 정보를 조회합니다. 회원 ID, 이메일, 닉네임, 자기소개, 생성일시 등의 정보를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 요청 (토큰 없음 또는 만료)")
    @GetMapping("/me")
    public MemberResponse getCurrentUser(@AuthenticationPrincipal MemberPrincipal principal) {
        return getMember(principal.id());
    }

    @Operation(
        summary = "회원 상세 정보 조회", 
        description = "특정 회원의 상세 정보를 조회합니다. 회원 ID, 이메일, 닉네임, 소개글, 프로필 설정(타임존, 로케일, 테마) 등을 포함합니다."
    )
    @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    @GetMapping("/{memberId}")
    @PreAuthorize("#memberId == authentication.principal.id or hasRole('ADMIN')")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        MemberResult dto = memberService.getMember(memberId);
        Profile profile = profileService.getProfile(memberId);
        return mapper.toResponse(dto, profile);
    }

    @Operation(
        summary = "회원 기본 정보 수정", 
        description = "회원의 닉네임과 소개글을 수정합니다. 본인만 수정 가능합니다.\n\n" +
                       "수정 가능한 필드:\n" +
                       "- nickname: 닉네임 (2-20자)\n" +
                       "- introduction: 소개글 (최대 500자)"
    )
    @ApiResponse(responseCode = "204", description = "회원 정보 수정 성공")
    @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 정보 수정 시도")
    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    @PatchMapping(value = "/{memberId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = {
            @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UpdateMemberRequest.class)),
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateMemberRequest.class))
        }
    )
    public void updateMember(
            @PathVariable UUID memberId,
            @Valid UpdateMemberRequest request) {
        UpdateMemberCommand command = mapper.toCommand(memberId, request);
        memberService.updateMember(command);
    }
    
    
    
    
    
    @Operation(
        summary = "비활성 회원 조회", 
        description = "오랜 기간 로그인하지 않은 비활성 회원 목록을 조회합니다. 기본값은 90일 이상 미접속 회원입니다. (관리자 전용)"
    )
    @ApiResponse(responseCode = "200", description = "비활성 회원 조회 성공")
    @GetMapping("/inactive")
    @ValidPageable(sortFields = SortFieldsProvider.MEMBER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<MemberResponse>> getInactiveMembers(
            @Parameter(description = "비활성 기준 일수") @RequestParam(defaultValue = "90") int days,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<Member> result = memberSearchService.getInactiveMembers(days, pageable);
        return ResponseEntity.ok(PageResponse.of(result.map(mapper::toResponse)));
    }
}
