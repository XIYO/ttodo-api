/**
 * Category 도메인의 유효성 검증 애노테이션 및 검증자
 * <p>
 * 이 패키지에는 Category 엔티티와 관련된 사용자 정의 유효성 검증 애노테이션과
 * 그 구현체들이 포함되어 있습니다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *     <li>{@link point.ttodoApi.category.domain.validation.ValidCategoryName} - 카테고리 이름 유효성 검증</li>
 *     <li>{@link point.ttodoApi.category.domain.validation.ValidColor} - 색상 유효성 검증</li>
 *     <li>{@link point.ttodoApi.category.domain.validation.ValidDescription} - 설명 유효성 검증</li>
 *     <li>{@link point.ttodoApi.category.domain.validation.ValidInvitationMessage} - 초대 메시지 유효성 검증</li>
 * </ul>
 *
 * @since 1.0
 * @see point.ttodoApi.category.domain.Category
 * @see point.ttodoApi.category.domain.CategoryCollaborator
 */
package point.ttodoApi.category.domain.validation;
