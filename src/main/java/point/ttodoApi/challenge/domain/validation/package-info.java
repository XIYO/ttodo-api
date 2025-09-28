/**
 * Challenge 도메인 커스텀 검증 어노테이션
 * 
 * <p>챌린진, 리더십, 참여 및 할일 엔티티에 대한 커스텀 유효성 검증을 제공합니다.
 * 
 * <h3>주요 커스텀 검증기:</h3>
 * <ul>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidChallengeTitle} - 챌린지 제목 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidChallengeDescription} - 챌린지 설명 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidInviteCode} - 초대 코드 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidMaxParticipants} - 최대 참여자 수 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidChallengeDate} - 챌린지 날짜 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidChallengePeriod} - 챌린지 기간 검증</li>
 *   <li>{@link point.ttodoApi.challenge.domain.validation.ValidRemovalReason} - 리더 제거 사유 검증</li>
 * </ul>
 */
package point.ttodoApi.challenge.domain.validation;