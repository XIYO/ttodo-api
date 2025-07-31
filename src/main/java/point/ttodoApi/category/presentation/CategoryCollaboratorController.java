package point.ttodoApi.category.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.category.application.CategoryCollaboratorService;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.dto.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 카테고리 협업자 관리 컨트롤러
 */
@RestController
@RequestMapping("/categories/{categoryId}/collaborators")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CategoryCollaborator", description = "카테고리 협업자 관리 API")
public class CategoryCollaboratorController {
    
    private final CategoryCollaboratorService collaboratorService;
    
    @PostMapping("/invite")
    @Operation(summary = "협업자 초대", description = "카테고리에 새로운 협업자를 초대합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업자 초대 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 초대된 멤버, 존재하지 않는 카테고리/멤버 등)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 owner가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<CollaboratorResponse>> inviteCollaborator(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Valid @RequestBody CollaboratorInviteRequest request) {
        
        CategoryCollaborator collaborator = collaboratorService.inviteCollaborator(
            categoryId, 
            request.getMemberId(), 
            request.getInvitationMessage()
        );
        
        CollaboratorResponse response = CollaboratorResponse.from(collaborator);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(response));
    }
    
    @PostMapping("/{memberId}/accept")
    @Operation(summary = "협업 초대 수락", description = "받은 협업 초대를 수락합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초대 수락 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (초대가 없거나 이미 처리됨)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<CollaboratorResponse>> acceptInvitation(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        CategoryCollaborator collaborator = collaboratorService.acceptInvitation(categoryId, memberId);
        CollaboratorResponse response = CollaboratorResponse.from(collaborator);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(response));
    }
    
    @PostMapping("/{memberId}/reject")
    @Operation(summary = "협업 초대 거절", description = "받은 협업 초대를 거절합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초대 거절 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (초대가 없거나 이미 처리됨)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<CollaboratorResponse>> rejectInvitation(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        CategoryCollaborator collaborator = collaboratorService.rejectInvitation(categoryId, memberId);
        CollaboratorResponse response = CollaboratorResponse.from(collaborator);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(response));
    }
    
    @DeleteMapping("/{memberId}")
    @Operation(summary = "협업자 제거", description = "카테고리에서 협업자를 제거합니다. (카테고리 owner만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업자 제거 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 owner가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<Void>> removeCollaborator(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Parameter(description = "제거할 멤버 ID") @PathVariable UUID memberId,
            @Parameter(description = "요청자 ID") @RequestParam UUID requesterId) {
        
        collaboratorService.removeCollaborator(categoryId, memberId, requesterId);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(null));
    }
    
    @PostMapping("/leave")
    @Operation(summary = "협업 나가기", description = "협업자가 스스로 협업에서 나갑니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업 나가기 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<Void>> leaveCollaboration(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Parameter(description = "멤버 ID") @RequestParam UUID memberId) {
        
        collaboratorService.leaveCollaboration(categoryId, memberId);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "카테고리 협업자 목록 조회", description = "카테고리의 모든 협업자 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업자 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 owner 또는 협업자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<CollaboratorResponse>>> getCategoryCollaborators(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Parameter(description = "요청자 ID") @RequestParam UUID requesterId) {
        
        List<CategoryCollaborator> collaborators = collaboratorService.getCategoryCollaborators(categoryId, requesterId);
        List<CollaboratorResponse> responses = collaborators.stream()
            .map(CollaboratorResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(responses));
    }
}

@RestController
@RequestMapping("/members/{memberId}/collaborations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MemberCollaboration", description = "멤버 협업 관리 API")
class MemberCollaborationController {
    
    private final CategoryCollaboratorService collaboratorService;
    
    @GetMapping("/invitations")
    @Operation(summary = "대기 중인 초대 목록", description = "멤버의 대기 중인 협업 초대 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초대 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<CollaboratorResponse>>> getPendingInvitations(
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        List<CategoryCollaborator> invitations = collaboratorService.getPendingInvitations(memberId);
        List<CollaboratorResponse> responses = invitations.stream()
            .map(CollaboratorResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(responses));
    }
    
    @GetMapping("/categories")
    @Operation(summary = "협업 카테고리 목록", description = "멤버가 협업하는 카테고리 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "협업 카테고리 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<String>>> getCollaborativeCategories(
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        // 이 엔드포인트는 나중에 CategoryResponse DTO를 만들어서 개선 필요
        List<point.ttodoApi.category.domain.Category> categories = collaboratorService.getCollaborativeCategories(memberId);
        List<String> categoryNames = categories.stream()
            .map(point.ttodoApi.category.domain.Category::getName)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(categoryNames));
    }
}