package point.ttodoApi.shared.specification;

/**
 * 검색 연산자 enum
 */
public enum SearchOperator {
  /**
   * 동등 비교 (=)
   */
  EQUALS,

  /**
   * 부등 비교 (!=)
   */
  NOT_EQUALS,

  /**
   * 크다 (>)
   */
  GREATER_THAN,

  /**
   * 크거나 같다 (>=)
   */
  GREATER_THAN_OR_EQUALS,

  /**
   * 작다 (<)
   */
  LESS_THAN,

  /**
   * 작거나 같다 (<=)
   */
  LESS_THAN_OR_EQUALS,

  /**
   * 작거나 같다 (<=) - 별칭
   */
  LESS_THAN_OR_EQUAL,

  /**
   * 포함 (LIKE %value%)
   */
  LIKE,

  /**
   * 시작 (LIKE value%)
   */
  STARTS_WITH,

  /**
   * 끝 (LIKE %value)
   */
  ENDS_WITH,

  /**
   * IN 절
   */
  IN,

  /**
   * NOT IN 절
   */
  NOT_IN,

  /**
   * NULL 체크
   */
  IS_NULL,

  /**
   * NOT NULL 체크
   */
  IS_NOT_NULL,

  /**
   * BETWEEN 절
   */
  BETWEEN,

  /**
   * OR 그룹 - OR로 연결될 조건들의 그룹
   */
  OR_GROUP
}