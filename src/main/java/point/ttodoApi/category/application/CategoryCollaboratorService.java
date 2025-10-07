package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.*;
import point.ttodoApi.category.infrastructure.persistence.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.util.*;

/**
 * 카테고리 협업자 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryCollaboratorService {

  private final CategoryCollaboratorRepository collaboratorRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository UserRepository;

  /**
   * 협업자 초대
   */
  public CategoryCollaborator inviteCollaborator(UUID categoryId, UUID userId, String invitationMessage) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 이미 초대되었거나 협업자인지 확인
    if (collaboratorRepository.existsActiveInvitation(category, user)) {
      throw new IllegalArgumentException("User is already invited or is a collaborator");
    }

    CategoryCollaborator collaborator = category.addCollaborator(user, invitationMessage);
    return collaboratorRepository.save(collaborator);
  }

  /**
   * 협업 초대 수락
   */
  public CategoryCollaborator acceptInvitation(UUID categoryId, UUID userId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndUser(category, user)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

    if (collaborator.getStatus() != CollaboratorStatus.PENDING) {
      throw new IllegalArgumentException("Invitation is not pending");
    }

    collaborator.accept();
    return collaboratorRepository.save(collaborator);
  }

  /**
   * 협업 초대 거절
   */
  public CategoryCollaborator rejectInvitation(UUID categoryId, UUID userId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndUser(category, user)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

    if (collaborator.getStatus() != CollaboratorStatus.PENDING) {
      throw new IllegalArgumentException("Invitation is not pending");
    }

    collaborator.reject();
    return collaboratorRepository.save(collaborator);
  }

  /**
   * 협업자 제거 (카테고리 owner만 가능)
   */
  public void removeCollaborator(UUID categoryId, UUID userId, UUID requesterId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User requester = UserRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterId));

    // 카테고리 owner만 협업자 제거 가능
    if (!category.isOwner(requester)) {
      throw new IllegalArgumentException("Only category owner can remove collaborators");
    }

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndUser(category, user)
            .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

    // 소프트 삭제
    collaborator.softDelete();
    collaboratorRepository.save(collaborator);

    // TODO: Implement todo collaboration logic with new TodoDefinition/TodoInstance architecture
    // 해당 멤버의 협업 투두를 개인 투두로 전환하는 로직 필요
    log.info("Removed collaborator {} from category {}", userId, categoryId);
  }

  /**
   * 협업자 스스로 나가기
   */
  public void leaveCollaboration(UUID categoryId, UUID userId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndUser(category, user)
            .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

    // 소프트 삭제
    collaborator.softDelete();
    collaboratorRepository.save(collaborator);

    // TODO: Implement todo collaboration logic with new TodoDefinition/TodoInstance architecture
    // 해당 멤버의 협업 투두를 개인 투두로 전환하는 로직 필요
    log.info("User {} left collaboration in category {}", userId, categoryId);
  }

  /**
   * 멤버의 대기 중인 초대 목록 조회
   */
  @Transactional(readOnly = true)
  public List<CategoryCollaborator> getPendingInvitations(UUID userId) {
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    return collaboratorRepository.findPendingInvitationsByUser(user);
  }

  /**
   * 카테고리의 모든 협업자 조회 (owner만 가능)
   */
  @Transactional(readOnly = true)
  public List<CategoryCollaborator> getCategoryCollaborators(UUID categoryId, UUID requesterId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User requester = UserRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterId));

    // 카테고리 owner 또는 협업자만 목록 조회 가능
    if (!category.canManage(requester)) {
      throw new IllegalArgumentException("No permission to view collaborators");
    }

    return collaboratorRepository.findByCategory(category);
  }

  /**
   * 멤버가 협업하는 카테고리 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Category> getCollaborativeCategories(UUID userId) {
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    return collaboratorRepository.findCollaborativeCategoriesByUser(user);
  }

  /**
   * 카테고리 협업 상태 확인
   */
  @Transactional(readOnly = true)
  public boolean isCategoryCollaborative(UUID categoryId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    return category.isCollaborative();
  }

  /**
   * 멤버가 카테고리 협업자인지 확인
   */
  @Transactional(readOnly = true)
  public boolean isCollaborator(UUID categoryId, UUID userId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    return category.isCollaborator(user);
  }

  /**
   * 카테고리 협업자 수 동기화
   * 카테고리에서 모든 협업자가 제거된 경우 협업 투두들을 개인 투두로 전환
   */
  public void syncCategoryCollaboration(UUID categoryId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    // TODO: Implement todo collaboration logic with new TodoDefinition/TodoInstance architecture
    // 활성 협업자가 없는 경우 모든 협업 투두를 개인 투두로 전환하는 로직 필요
    if (!category.isCollaborative()) {
      log.info("Category {} has no collaborators, sync logic needed for new architecture", categoryId);
    }
  }
}