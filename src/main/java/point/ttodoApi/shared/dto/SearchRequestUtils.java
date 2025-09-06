package point.ttodoApi.shared.dto;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 검색 요청 처리를 위한 유틸리티
 */
@Component
public class SearchRequestUtils {

  /**
   * BaseSearchRequest를 Spring Data Pageable로 변환
   *
   * @param request 검색 요청
   * @return Pageable 객체
   */
  public Pageable toPageable(BaseSearchRequest request) {
    if (request == null) {
      return PageRequest.of(0, 20);
    }

    // 검증
    request.validate();

    // 정렬 조건 파싱
    Sort sort = parseSort(request.finalSort());

    // Pageable 생성
    return PageRequest.of(
            request.getPage(),
            request.getSize(),
            sort
    );
  }

  /**
   * 정렬 문자열을 Sort 객체로 변환
   * 형식: "field1,asc;field2,desc"
   *
   * @param sortString 정렬 문자열
   * @return Sort 객체
   */
  private Sort parseSort(String sortString) {
    if (sortString == null || sortString.trim().isEmpty()) {
      return Sort.unsorted();
    }

    List<Sort.Order> orders = new ArrayList<>();
    String[] sortPairs = sortString.split(";");

    for (String sortPair : sortPairs) {
      String[] parts = sortPair.trim().split(",");
      if (parts.length == 2) {
        String field = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();

        if ("ASC".equals(direction)) {
          orders.add(Sort.Order.asc(field));
        } else if ("DESC".equals(direction)) {
          orders.add(Sort.Order.desc(field));
        }
      }
    }

    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
  }

  /**
   * 검색 키워드 정제
   * - 앞뒤 공백 제거
   * - SQL 와일드카드 이스케이프
   *
   * @param keyword 원본 키워드
   * @return 정제된 키워드
   */
  public String sanitizeKeyword(String keyword) {
    if (keyword == null) {
      return null;
    }

    // 공백 제거
    keyword = keyword.trim();

    // 빈 문자열 체크
    if (keyword.isEmpty()) {
      return null;
    }

    // SQL 와일드카드 이스케이프
    keyword = keyword.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");

    return keyword;
  }

  /**
   * Like 검색을 위한 패턴 생성
   *
   * @param keyword   키워드
   * @param matchType 매치 타입
   * @return Like 패턴
   */
  public String toLikePattern(String keyword, MatchType matchType) {
    if (keyword == null || keyword.isEmpty()) {
      return null;
    }

    String sanitized = sanitizeKeyword(keyword);
    if (sanitized == null) {
      return null;
    }

    return switch (matchType) {
      case STARTS_WITH -> sanitized + "%";
      case ENDS_WITH -> "%" + sanitized;
      case CONTAINS -> "%" + sanitized + "%";
      case EXACT -> sanitized;
    };
  }

  /**
   * 검색 매치 타입
   */
  public enum MatchType {
    STARTS_WITH,  // 시작 문자 매치
    ENDS_WITH,    // 끝 문자 매치
    CONTAINS,     // 포함 매치
    EXACT         // 정확한 매치
  }
}