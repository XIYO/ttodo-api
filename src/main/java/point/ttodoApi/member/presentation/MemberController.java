package point.ttodoApi.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.UpdateMemberCommand;
import point.ttodoApi.member.application.dto.result.MemberResult;
import point.ttodoApi.member.presentation.dto.request.UpdateMemberRequest;
import point.ttodoApi.member.presentation.dto.response.MemberResponse;
import point.ttodoApi.member.presentation.mapper.MemberPresentationMapper;
import point.ttodoApi.profile.application.ProfileService;
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
    private final ProfileService profileService;
    private final MemberPresentationMapper mapper;

    @Operation(
        summary = "전체 회원 목록 조회", 
        description = "시스템에 등록된 모든 회원 목록을 페이지네이션과 함께 조회합니다.\n\n" +
                       "페이지네이션 파라미터:\n" +
                       "- page: 페이지 번호 (0부터 시작, 기본값: 0)\n" +
                       "- size: 페이지 크기 (기본값: 10)\n" +
                       "- sort: 정렬 기준 (id, nickname, createdAt 등)"
    )
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    @GetMapping
    public Page<MemberResponse> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return memberService.getMembers(pageable)
                .map(mapper::toResponse);
    }

    @Operation(
        summary = "회원 상세 정보 조회", 
        description = "특정 회원의 상세 정보를 조회합니다. 회원 ID, 이메일, 닉네임, 소개글, 프로필 설정(타임존, 로케일, 테마) 등을 포함합니다."
    )
    @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    @GetMapping("/{memberId}")
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
        summary = "UI 테마 변경", 
        description = "사용자의 UI 테마를 변경합니다. 라이트 모드와 다크 모드를 지원합니다.\n\n" +
                       "사용 가능한 테마:\n" +
                       "- LIGHT: 라이트 모드\n" +
                       "- DARK: 다크 모드"
    )
    @ApiResponse(responseCode = "204", description = "테마 변경 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 테마 값")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 테마 변경 시도")
    @PatchMapping("/{memberId}/theme")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void updateTheme(
            @PathVariable UUID memberId,
            @RequestParam Theme theme) {
        profileService.updateTheme(memberId, theme);
    }
    
    @Operation(
        summary = "프로필 이미지 업로드", 
        description = "회원의 프로필 이미지를 업로드합니다. 기존 이미지가 있을 경우 대체됩니다.\n\n" +
                       "지원 형식:\n" +
                       "- JPEG, PNG, GIF\n" +
                       "- 최대 크기: 5MB\n" +
                       "- 권장 크기: 200x200 픽셀 이상"
    )
    @ApiResponse(responseCode = "204", description = "프로필 이미지 업로드 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 크기 초과")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지 업로드 시도")
    @PostMapping(value = "/{memberId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void uploadProfileImage(
            @PathVariable UUID memberId,
            @RequestParam("image") MultipartFile image) throws IOException {
        profileService.updateProfileImage(memberId, image);
    }
    
    @Operation(
        summary = "프로필 이미지 조회", 
        description = "회원의 프로필 이미지를 직접 조회합니다. 이미지는 1시간 동안 브라우저에 캐시됩니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 이미지 조회 성공", 
                 content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "프로필 이미지가 없습니다.")
    @GetMapping(value = "/{memberId}/profile-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<byte[]> getProfileImage(@PathVariable UUID memberId) {
        Profile profile = profileService.getProfile(memberId);
        if (profile.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(profile.getProfileImageType()))
                .header("Cache-Control", "public, max-age=3600") // 1시간 캐싱
                .body(profile.getProfileImage());
    }
    
    @Operation(
        summary = "프로필 이미지 삭제", 
        description = "회원의 프로필 이미지를 삭제합니다. 삭제 후에는 기본 프로필 이미지가 표시됩니다."
    )
    @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지 삭제 시도")
    @DeleteMapping("/{memberId}/profile-image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void deleteProfileImage(@PathVariable UUID memberId) {
        profileService.removeProfileImage(memberId);
    }
}