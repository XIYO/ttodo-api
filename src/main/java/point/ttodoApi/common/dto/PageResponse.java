package point.ttodoApi.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 페이지 응답을 위한 공통 DTO
 * Spring Data의 Page 객체를 클라이언트 친화적인 형태로 변환
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지 응답")
public class PageResponse<T> {
    
    @Schema(description = "현재 페이지의 데이터 목록")
    private List<T> content;
    
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int page;
    
    @Schema(description = "페이지 크기", example = "20")
    private int size;
    
    @Schema(description = "전체 요소 수", example = "100")
    private long totalElements;
    
    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;
    
    @Schema(description = "첫 페이지 여부", example = "true")
    private boolean first;
    
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean last;
    
    @Schema(description = "현재 페이지의 요소 수", example = "20")
    private int numberOfElements;
    
    @Schema(description = "빈 페이지 여부", example = "false")
    private boolean empty;
    
    /**
     * Spring Data Page 객체로부터 PageResponse 생성
     * 
     * @param page Spring Data Page 객체
     * @param <T> 데이터 타입
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
    
    /**
     * Spring Data Page 객체로부터 변환된 PageResponse 생성
     * 
     * @param page Spring Data Page 객체
     * @param converter 변환 함수
     * @param <U> 원본 데이터 타입
     * @param <T> 변환된 데이터 타입
     * @return 변환된 PageResponse 객체
     */
    public static <U, T> PageResponse<T> of(Page<U> page, Function<U, T> converter) {
        List<T> convertedContent = page.getContent()
                .stream()
                .map(converter)
                .collect(Collectors.toList());
        
        return PageResponse.<T>builder()
                .content(convertedContent)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
    
    /**
     * 다음 페이지가 있는지 확인
     */
    public boolean hasNext() {
        return !last;
    }
    
    /**
     * 이전 페이지가 있는지 확인
     */
    public boolean hasPrevious() {
        return !first;
    }
    
    /**
     * 다음 페이지 번호 반환
     */
    public Integer getNextPage() {
        return hasNext() ? page + 1 : null;
    }
    
    /**
     * 이전 페이지 번호 반환
     */
    public Integer getPreviousPage() {
        return hasPrevious() ? page - 1 : null;
    }
}