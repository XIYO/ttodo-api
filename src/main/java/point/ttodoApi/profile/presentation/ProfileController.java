package point.ttodoApi.profile.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.UpdateMemberCommand;
import point.ttodoApi.member.application.dto.result.MemberResult;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.presentation.dto.request.UpdateProfileRequest;
import point.ttodoApi.profile.presentation.dto.response.*;

import java.io.IOException;
import java.util.UUID;

/**
 * 프로필 관련 API 컨트롤러
 */
@Tag(name = "프로필(Profile) 설정", description = "사용자 프로필 정보를 관리하는 API입니다. 닉네임, 자기소개, 프로필 이미지, 타임존, 로케일, 테마 등 개인화 설정을 포함합니다. 타임존과 로케일 설정은 할 일 표시 시간과 언어에 영향을 줍니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members/{memberId}/profile")
public class ProfileController {

    private final MemberService memberService;
    private final ProfileService profileService;

    @Operation(
        summary = "프로필 정보 조회", 
        description = "회원의 프로필 정보를 조회합니다. 닉네임, 자기소개, 타임존, 로케일, 테마, 프로필 이미지 URL을 포함합니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 정보 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ProfileResponse getProfile(@PathVariable UUID memberId) {
        MemberResult memberResult = memberService.getMember(memberId);
        Profile profile = profileService.getProfile(memberId);
        
        return new ProfileResponse(
                memberResult.nickname(),
                profile.getIntroduction(),
                profile.getTimeZone(),
                profile.getLocale(),
                profile.getTheme(),
                profile.getImageUrl()
        );
    }

    @Operation(
        summary = "프로필 정보 수정", 
        description = "회원의 프로필 정보를 부분 수정합니다. 본인만 수정 가능합니다.\n\n" +
                       "수정 가능한 필드:\n" +
                       "- nickname: 닉네임\n" +
                       "- introduction: 자기소개\n" +
                       "- timeZone: 타임존 (Asia/Seoul, America/New_York 등)\n" +
                       "- locale: 로케일 (ko_KR, en_US 등)\n" +
                       "- theme: 테마 (LIGHT, DARK)"
    )
    @ApiResponse(responseCode = "204", description = "프로필 정보 수정 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 타임존, 로케일, 테마 값")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 수정 시도")
    @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    @PatchMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void updateProfile(
            @PathVariable UUID memberId,
            @Valid UpdateProfileRequest request) {
        
        // 회원 기본 정보 업데이트 (닉네임만)
        if (request.nickname() != null) {
            UpdateMemberCommand command = new UpdateMemberCommand(
                    memberId,
                    request.nickname(),
                    null // introduction은 더 이상 Member에서 관리하지 않음
            );
            memberService.updateMember(command);
        }
        
        // 프로필 정보 업데이트
        Profile profile = profileService.getProfile(memberId);
        boolean profileUpdated = false;
        
        if (request.introduction() != null) {
            profile.updateIntroduction(request.introduction());
            profileUpdated = true;
        }
        
        if (request.timeZone() != null) {
            profile.updateTimeZone(request.timeZone());
            profileUpdated = true;
        }
        
        if (request.locale() != null) {
            profile.updateLocale(request.locale());
            profileUpdated = true;
        }
        
        if (request.theme() != null) {
            profile.updateTheme(request.theme());
            profileUpdated = true;
        }
        
        if (profileUpdated) {
            profileService.saveProfile(profile);
        }
    }

    @Operation(
        summary = "프로필 이미지 조회", 
        description = "회원의 프로필 이미지를 바이너리 데이터로 직접 조회합니다. 이미지는 1시간 동안 브라우저에 캐시됩니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 이미지 조회 성공", 
                 content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "프로필 이미지가 없습니다.")
    @GetMapping(value = "/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public byte[] getProfileImage(@PathVariable UUID memberId, HttpServletResponse response) {
        Profile profile = profileService.getProfile(memberId);
        if (profile.getProfileImage() == null) {
            throw new point.ttodoApi.shared.error.NotFoundException("프로필 이미지가 없습니다.");
        }
        response.setContentType(profile.getProfileImageType());
        response.setHeader("Cache-Control", "public, max-age=3600"); // 1시간 캐싱
        return profile.getProfileImage();
    }

    @Operation(
        summary = "프로필 이미지 업로드", 
        description = "회원의 프로필 이미지를 업로드합니다. 기존 이미지가 있을 경우 대체됩니다.\n\n" +
                       "지원 형식:\n" +
                       "- JPEG, PNG, GIF\n" +
                       "- 최대 크기: 5MB\n" +
                       "- 권장 크기: 200x200 픽셀 이상"
    )
    @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공, 이미지 URL 반환")
    @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 크기 초과")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지 업로드 시도")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("#memberId == authentication.principal.id")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    public ProfileImageUploadResponse uploadProfileImage(
            @PathVariable UUID memberId,
            @RequestParam("image") MultipartFile image) throws IOException {
        Profile updatedProfile = profileService.updateProfileImage(memberId, image);
        return new ProfileImageUploadResponse(updatedProfile.getImageUrl());
    }

    @Operation(
        summary = "프로필 이미지 삭제", 
        description = "회원의 프로필 이미지를 삭제합니다. 삭제 후에는 기본 프로필 이미지가 표시됩니다."
    )
    @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공")
    @ApiResponse(responseCode = "403", description = "다른 사용자의 프로필 이미지 삭제 시도")
    @DeleteMapping("/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#memberId == authentication.principal.id")
    public void deleteProfileImage(@PathVariable UUID memberId) {
        profileService.removeProfileImage(memberId);
    }
}