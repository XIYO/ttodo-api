package point.ttodoApi.shared.specification;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import point.ttodoApi.shared.error.BusinessException;

import java.time.*;
import java.util.*;

@Slf4j
public abstract class BaseSpecification<T> {

  // 기본 엔티티 필드 (대부분의 엔티티가 공통으로 가지는 필드)
  protected static final Set<String> COMMON_SORT_FIELDS = Set.of(
          "id", "createdAt", "updatedAt"
  );

  // 허용된 정렬 필드를 각 구현체에서 정의
  protected abstract Set<String> getAllowedSortFields();

  /**
   * 안전한 동적 쿼리 구성을 위한 기본 메서드
   */
  protected Specification<T> buildSpecification(List<SearchCriteria> criteriaList) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      for (SearchCriteria criteria : criteriaList) {
        Predicate predicate = buildPredicate(criteria, root, criteriaBuilder);
        if (predicate != null) {
          predicates.add(predicate);
        }
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * 단일 검색 조건을 Predicate로 변환
   */
  protected Predicate buildPredicate(SearchCriteria criteria, Root<T> root, CriteriaBuilder cb) {
    String fieldName = sanitizeFieldName(criteria.getKey());
    Path<?> path = getFieldPath(root, fieldName);

    if (path == null) {
      log.warn("Invalid field name: {}", fieldName);
      return null;
    }

    Object value = convertValue(criteria.getValue(), path.getJavaType());

    return switch (criteria.getOperation()) {
      case EQUALS -> cb.equal(path, value);
      case NOT_EQUALS -> cb.notEqual(path, value);
      case GREATER_THAN -> cb.greaterThan(path.as(Comparable.class), (Comparable) value);
      case GREATER_THAN_OR_EQUALS -> cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
      case LESS_THAN -> cb.lessThan(path.as(Comparable.class), (Comparable) value);
      case LESS_THAN_OR_EQUALS, LESS_THAN_OR_EQUAL ->
              cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) value);
      case LIKE -> cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
      case STARTS_WITH -> cb.like(cb.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
      case ENDS_WITH -> cb.like(cb.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
      case IN -> path.in((Collection<?>) value);
      case NOT_IN -> cb.not(path.in((Collection<?>) value));
      case IS_NULL -> cb.isNull(path);
      case IS_NOT_NULL -> cb.isNotNull(path);
      case BETWEEN -> {
        if (value instanceof List<?> list && list.size() == 2) {
          yield cb.between(path.as(Comparable.class),
                  (Comparable) list.get(0),
                  (Comparable) list.get(1));
        }
        throw new IllegalArgumentException("BETWEEN requires exactly 2 values");
      }
      case OR_GROUP -> {
        if (value instanceof List<?> orCriteriaList) {
          List<Predicate> orPredicates = new ArrayList<>();
          for (Object item : orCriteriaList) {
            if (item instanceof SearchCriteria orCriteria) {
              Predicate orPredicate = buildPredicate(orCriteria, root, cb);
              if (orPredicate != null) {
                orPredicates.add(orPredicate);
              }
            }
          }
          yield cb.or(orPredicates.toArray(new Predicate[0]));
        }
        throw new IllegalArgumentException("OR_GROUP requires a list of SearchCriteria");
      }
    };
  }

  /**
   * 필드 경로 가져오기 (중첩 필드 지원)
   */
  protected Path<?> getFieldPath(Root<T> root, String fieldName) {
    if (fieldName == null || fieldName.isEmpty()) {
      return null;
    }

    // SQL Injection 방지를 위한 필드명 검증
    if (!isValidFieldName(fieldName)) {
      return null;
    }

    try {
      String[] parts = fieldName.split("\\.");
      Path<?> path = root;

      for (String part : parts) {
        path = path.get(part);
      }

      return path;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid field path: {}", fieldName, e);
      return null;
    }
  }

  /**
   * 필드명 검증 (알파벳, 숫자, 언더스코어, 점만 허용)
   */
  protected boolean isValidFieldName(String fieldName) {
    return fieldName.matches("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$");
  }

  /**
   * 필드명 sanitization
   */
  protected String sanitizeFieldName(String fieldName) {
    if (fieldName == null) {
      return null;
    }
    // 위험한 문자 제거
    return fieldName.replaceAll("[^a-zA-Z0-9._]", "");
  }

  /**
   * 값 타입 변환
   */
  protected Object convertValue(Object value, Class<?> targetType) {
    if (value == null || targetType == null) {
      return value;
    }

    try {
      if (targetType.equals(LocalDate.class) && value instanceof String) {
        return LocalDate.parse((String) value);
      }
      if (targetType.equals(LocalDateTime.class) && value instanceof String) {
        return LocalDateTime.parse((String) value);
      }
      if (targetType.equals(Integer.class) && value instanceof String) {
        return Integer.parseInt((String) value);
      }
      if (targetType.equals(Long.class) && value instanceof String) {
        return Long.parseLong((String) value);
      }
      if (targetType.equals(Boolean.class) && value instanceof String) {
        return Boolean.parseBoolean((String) value);
      }
      if (targetType.equals(UUID.class) && value instanceof String) {
        return UUID.fromString((String) value);
      }

      return value;
    } catch (Exception e) {
      log.error("Failed to convert value: {} to type: {}", value, targetType, e);
      throw new BusinessException("Invalid value format for field");
    }
  }

  /**
   * 정렬 필드 검증
   */
  public void validateSortField(String sortField) {
    if (sortField == null || sortField.isEmpty()) {
      return;
    }

    String sanitizedField = sanitizeFieldName(sortField);
    Set<String> allowedFields = getAllowedSortFields();

    if (!allowedFields.contains(sanitizedField)) {
      throw new BusinessException("Invalid sort field: " + sortField);
    }
  }

  /**
   * 여러 정렬 필드 검증
   */
  public void validateSortFields(List<String> sortFields) {
    if (sortFields == null || sortFields.isEmpty()) {
      return;
    }

    sortFields.forEach(this::validateSortField);
  }
}