/**
 * Todo 도메인 유효성 검증 애노테이션 및 Validator 클래스
 * 
 * <p>이 패키지는 Todo 도메인 엔티티의 데이터 유효성을 검증하기 위한
 * 커스텀 애노테이션과 검증기를 포함합니다.</p>
 * 
 * <h3>주요 애노테이션:</h3>
 * <ul>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidTitle} - Todo 제목 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidDescription} - Todo 설명 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidTags} - 태그 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidTodoPriority} - 우선순위 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidDisplayOrder} - 순서 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidTodoDate} - 날짜 검증</li>
 *     <li>{@link point.ttodoApi.todo.domain.validation.ValidOwner} - 소유자 검증</li>
 * </ul>
 * 
 * @since 1.0
 * @author TTODO Development Team
 */
package point.ttodoApi.todo.domain.validation;
