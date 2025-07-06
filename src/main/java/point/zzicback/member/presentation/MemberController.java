package point.zzicback.member.presentation;

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
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.result.MemberResult;
import point.zzicback.member.presentation.dto.request.UpdateMemberRequest;
import point.zzicback.member.presentation.dto.response.MemberResponse;
import point.zzicback.member.presentation.mapper.MemberPresentationMapper;
import point.zzicback.profile.application.MemberProfileService;
import point.zzicback.profile.domain.MemberProfile;
import point.zzicback.profile.domain.Theme;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * 회원 관련 Presentation 레이어 API 컨트롤러
 */
@Tag(name = "멤버", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;
    private final MemberPresentationMapper mapper;

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징 조회합니다.")
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

    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        MemberResult dto = memberService.getMember(memberId);
        MemberProfile profile = memberProfileService.getProfile(memberId);
        return mapper.toResponse(dto, profile);
    }

    @Operation(summary = "회원 정보 수정", description = "회원 닉네임과 소개글을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "회원 정보 수정 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 정보는 수정할 수 없습니다.")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
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
    
    @Operation(summary = "회원 테마 변경", description = "회원의 테마를 변경합니다.")
    @ApiResponse(responseCode = "204", description = "테마 변경 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 테마는 변경할 수 없습니다.")
    @PatchMapping("/{memberId}/theme")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void updateTheme(
            @PathVariable UUID memberId,
            @RequestParam Theme theme) {
        memberProfileService.updateTheme(memberId, theme);
    }
    
    @Operation(summary = "회원 프로필 이미지 업로드", description = "회원의 프로필 이미지를 업로드합니다.")
    @ApiResponse(responseCode = "204", description = "프로필 이미지 업로드 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지는 업로드할 수 없습니다.")
    @PostMapping(value = "/{memberId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void uploadProfileImage(
            @PathVariable UUID memberId,
            @RequestParam("image") MultipartFile image) throws IOException {
        memberProfileService.updateProfileImage(memberId, image);
    }
    
    @Operation(summary = "회원 프로필 이미지 조회", description = "회원의 프로필 이미지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지 조회 성공")
    @ApiResponse(responseCode = "404", description = "프로필 이미지가 없습니다.")
    @GetMapping("/{memberId}/profile-image")
    public ResponseEntity<String> getProfileImage(@PathVariable UUID memberId) {
        MemberProfile profile = memberProfileService.getProfile(memberId);
        if (profile.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }
        String base64Image = Base64.getEncoder().encodeToString(profile.getProfileImage());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(profile.getProfileImageType()))
                .body(base64Image);
    }
    
    @Operation(summary = "회원 프로필 이미지 삭제", description = "회원의 프로필 이미지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지는 삭제할 수 없습니다.")
    @DeleteMapping("/{memberId}/profile-image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void deleteProfileImage(@PathVariable UUID memberId) {
        memberProfileService.removeProfileImage(memberId);
    }
}