package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.*;
import point.ttodoApi.category.infrastructure.persistence.*;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

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
  private final MemberRepository memberRepository;
  private final TodoRepository todoRepository;

  /**
   * 협업자 초대
   */
  public CategoryCollaborator inviteCollaborator(UUID categoryId, UUID memberId, String invitationMessage) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    // 이미 초대되었거나 협업자인지 확인
    if (collaboratorRepository.existsActiveInvitation(category, member)) {
      throw new IllegalArgumentException("Member is already invited or is a collaborator");
    }

    CategoryCollaborator collaborator = category.addCollaborator(member, invitationMessage);
    return collaboratorRepository.save(collaborator);
  }

  /**
   * 협업 초대 수락
   */
  public CategoryCollaborator acceptInvitation(UUID categoryId, UUID memberId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndMember(category, member)
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
  public CategoryCollaborator rejectInvitation(UUID categoryId, UUID memberId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndMember(category, member)
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
  public void removeCollaborator(UUID categoryId, UUID memberId, UUID requesterId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member requester = memberRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterId));

    // 카테고리 owner만 협업자 제거 가능
    if (!category.isOwner(requester)) {
      throw new IllegalArgumentException("Only category owner can remove collaborators");
    }

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndMember(category, member)
            .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

    // 소프트 삭제
    collaborator.softDelete();
    collaboratorRepository.save(collaborator);

    // 해당 멤버의 협업 투두를 개인 투두로 전환
    int updatedTodos = todoRepository.updateMemberCollaborativeTodosToPersonal(memberId, categoryId);
    log.info("Converted {} collaborative todos to personal for member {} in category {}",
            updatedTodos, memberId, categoryId);
  }

  /**
   * 협업자 스스로 나가기
   */
  public void leaveCollaboration(UUID categoryId, UUID memberId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    CategoryCollaborator collaborator = collaboratorRepository.findByCategoryAndMember(category, member)
            .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));

    // 소프트 삭제
    collaborator.softDelete();
    collaboratorRepository.save(collaborator);

    // 해당 멤버의 협업 투두를 개인 투두로 전환
    int updatedTodos = todoRepository.updateMemberCollaborativeTodosToPersonal(memberId, categoryId);
    log.info("Member {} left collaboration in category {}, converted {} todos to personal",
            memberId, categoryId, updatedTodos);
  }

  /**
   * 멤버의 대기 중인 초대 목록 조회
   */
  @Transactional(readOnly = true)
  public List<CategoryCollaborator> getPendingInvitations(UUID memberId) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    return collaboratorRepository.findPendingInvitationsByMember(member);
  }

  /**
   * 카테고리의 모든 협업자 조회 (owner만 가능)
   */
  @Transactional(readOnly = true)
  public List<CategoryCollaborator> getCategoryCollaborators(UUID categoryId, UUID requesterId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member requester = memberRepository.findById(requesterId)
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
  public List<Category> getCollaborativeCategories(UUID memberId) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    return collaboratorRepository.findCollaborativeCategoriesByMember(member);
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
  public boolean isCollaborator(UUID categoryId, UUID memberId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    return category.isCollaborator(member);
  }

  /**
   * 카테고리 협업자 수 동기화
   * 카테고리에서 모든 협업자가 제거된 경우 협업 투두들을 개인 투두로 전환
   */
  public void syncCategoryCollaboration(UUID categoryId) {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

    // 활성 협업자가 없는 경우 모든 협업 투두를 개인 투두로 전환
    if (!category.isCollaborative()) {
      int updatedTodos = todoRepository.updateCollaborativeTodosToPersonalByCategoryId(categoryId);
      log.info("Category {} has no collaborators, converted {} collaborative todos to personal",
              categoryId, updatedTodos);
    }
  }
}