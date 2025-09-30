package point.ttodoApi.category.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.category.application.CategoryCollaboratorService;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.presentation.dto.request.*;
import point.ttodoApi.category.presentation.dto.response.*;
import point.ttodoApi.category.presentation.mapper.CategoryCollaboratorPresentationMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 카테고리 협업자 관리 컨트롤러
 */
@RestController
@RequestMapping("/categories/{categoryId}/collaborators")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "카테고리 협업자 관리", description = "카테고리를 여러 사용자가 함께 사용할 수 있도록 협업자를 초대하고 관리합니다. 협업 초대, 수락, 거절, 제거 기능을 제공합니다.")
public class CategoryCollaboratorController {

  private final CategoryCollaboratorService collaboratorService;
  private final CategoryCollaboratorPresentationMapper collaboratorMapper;

  @PostMapping(consumes = {org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(#categoryId, 'Category', 'WRITE')")
  @Operation(summary = "협업자 초대", description = "카테고리에 새로운 협업자를 초대합니다. 카테고리 소유자만 협업자를 초대할 수 있습니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "협업자 초대 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 초대된 멤버, 존재하지 않는 카테고리 또는 멤버)"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 소유자가 아님)")
  })
  public CollaboratorResponse inviteCollaborator(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @Valid CollaboratorInviteRequest request) {

    CategoryCollaborator collaborator = collaboratorService.inviteCollaborator(
            categoryId,
            request.getUserId(),
            request.getInvitationMessage()
    );

    return collaboratorMapper.toResponse(collaborator);
  }

  @PostMapping("/accept")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "협업 초대 수락", description = "받은 협업 초대를 수락하여 카테고리 협업자가 됩니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "초대 수락 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (대기 중인 초대가 없거나 이미 처리됨)"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public CollaboratorResponse acceptInvitation(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    CategoryCollaborator collaborator = collaboratorService.acceptInvitation(categoryId, UUID.fromString(user.getUsername()));
    return collaboratorMapper.toResponse(collaborator);
  }

  @PostMapping("/reject")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "협업 초대 거절", description = "받은 협업 초대를 거절합니다. 거절한 초대는 삭제됩니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "초대 거절 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (대기 중인 초대가 없거나 이미 처리됨)"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public CollaboratorResponse rejectInvitation(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    CategoryCollaborator collaborator = collaboratorService.rejectInvitation(categoryId, UUID.fromString(user.getUsername()));
    return collaboratorMapper.toResponse(collaborator);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#categoryId, 'Category', 'WRITE')")
  @Operation(summary = "협업자 제거", description = "카테고리에서 협업자를 제거합니다. 카테고리 소유자만 협업자를 제거할 수 있습니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "협업자 제거 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 소유자가 아님)")
  })
  public void removeCollaborator(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @Parameter(description = "제거할 멤버 ID") @PathVariable UUID userId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    collaboratorService.removeCollaborator(categoryId, userId, UUID.fromString(user.getUsername()));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "협업 나가기", description = "협업자가 스스로 카테고리 협업에서 나갑니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "협업 나가기 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (협업자가 아님)"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public void leaveCollaboration(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    collaboratorService.leaveCollaboration(categoryId, UUID.fromString(user.getUsername()));
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasPermission(#categoryId, 'Category', 'READ')")
  @Operation(summary = "카테고리 협업자 목록 조회", description = "카테고리의 모든 협업자 목록을 조회합니다. 소유자와 현재 협업 중인 모든 멤버 정보를 포함합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "협업자 목록 조회 성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (카테고리 소유자 또는 협업자가 아님)")
  })
  public List<CollaboratorResponse> getCategoryCollaborators(
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    List<CategoryCollaborator> collaborators = collaboratorService.getCategoryCollaborators(categoryId, UUID.fromString(user.getUsername()));
    return collaborators.stream()
            .map(collaboratorMapper::toResponse)
            .collect(Collectors.toList());
  }
}

@RestController
@RequestMapping("/user/{userId}/collaborations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "멤버별 협업 관리", description = "특정 멤버의 협업 초대 현황과 참여 중인 협업 카테고리를 관리합니다.")
class UserCollaborationController {

  private final CategoryCollaboratorService collaboratorService;
  private final CategoryCollaboratorPresentationMapper collaboratorMapper;

  @GetMapping("/invitations")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "대기 중인 협업 초대 목록 조회", description = "멤버가 받은 협업 초대 중 아직 수락하거나 거절하지 않은 대기 중인 초대 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "초대 목록 조회 성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @org.springframework.security.access.prepost.PreAuthorize("#userId == authentication.principal.id")
  public List<CollaboratorResponse> getPendingInvitations(
          @Parameter(description = "멤버 ID") @PathVariable UUID userId) {

    List<CategoryCollaborator> invitations = collaboratorService.getPendingInvitations(userId);
    return invitations.stream()
            .map(collaboratorMapper::toResponse)
            .collect(Collectors.toList());
  }

  @GetMapping("/categories")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "참여 중인 협업 카테고리 목록 조회", description = "멤버가 현재 협업자로 참여하고 있는 모든 카테고리 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "협업 카테고리 목록 조회 성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @org.springframework.security.access.prepost.PreAuthorize("#userId == authentication.principal.id")
  public List<String> getCollaborativeCategories(
          @Parameter(description = "멤버 ID") @PathVariable UUID userId) {

    // 이 엔드포인트는 나중에 CategoryResponse DTO를 만들어서 개선 필요
    List<point.ttodoApi.category.domain.Category> categories = collaboratorService.getCollaborativeCategories(userId);
    return categories.stream()
            .map(point.ttodoApi.category.domain.Category::getName)
            .collect(Collectors.toList());
  }
}
