package point.zzicback.profile.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.result.MemberResult;
import point.zzicback.profile.application.MemberProfileService;
import point.zzicback.profile.domain.MemberProfile;
import point.zzicback.profile.presentation.dto.request.UpdateProfileRequest;
import point.zzicback.profile.presentation.dto.response.ProfileResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * 프로필 관련 API 컨트롤러
 */
@Tag(name = "프로필", description = "프로필 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/profile")
public class ProfileController {

    private final MemberService memberService;
    private final MemberProfileService memberProfileService;

    @Operation(summary = "프로필 정보 조회", description = "회원의 프로필 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 정보 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @GetMapping
    public ProfileResponse getProfile(@PathVariable UUID memberId) {
        MemberResult memberResult = memberService.getMember(memberId);
        MemberProfile profile = memberProfileService.getProfile(memberId);
        
        return new ProfileResponse(
                memberResult.nickname(),
                memberResult.introduction(),
                profile.getTheme(),
                profile.getProfileImage() != null
        );
    }

    @Operation(summary = "프로필 정보 수정", description = "회원의 프로필 정보를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "프로필 정보 수정 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 정보는 수정할 수 없습니다.")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void updateProfile(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        // 회원 기본 정보 업데이트 (닉네임, 소개글)
        if (request.nickname() != null || request.introduction() != null) {
            UpdateMemberCommand command = new UpdateMemberCommand(
                    memberId,
                    request.nickname(),
                    request.introduction()
            );
            memberService.updateMember(command);
        }
        
        // 테마 업데이트
        if (request.theme() != null) {
            memberProfileService.updateTheme(memberId, request.theme());
        }
    }

    @Operation(summary = "프로필 이미지 조회", description = "회원의 프로필 이미지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지 조회 성공", 
                 content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "프로필 이미지가 없습니다.")
    @GetMapping(value = "/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<byte[]> getProfileImage(@PathVariable UUID memberId) {
        MemberProfile profile = memberProfileService.getProfile(memberId);
        if (profile.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(profile.getProfileImageType()))
                .header("Cache-Control", "public, max-age=3600") // 1시간 캐싱
                .body(profile.getProfileImage());
    }

    @Operation(summary = "프로필 이미지 업로드", description = "회원의 프로필 이미지를 업로드합니다.")
    @ApiResponse(responseCode = "204", description = "프로필 이미지 업로드 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지는 업로드할 수 없습니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 이미지 파일입니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    public void uploadProfileImage(
            @PathVariable UUID memberId,
            @RequestParam("image") MultipartFile image) throws IOException {
        memberProfileService.updateProfileImage(memberId, image);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "회원의 프로필 이미지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지는 삭제할 수 없습니다.")
    @DeleteMapping("/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void deleteProfileImage(@PathVariable UUID memberId) {
        memberProfileService.removeProfileImage(memberId);
    }
}