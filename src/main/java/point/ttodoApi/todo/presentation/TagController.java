package point.ttodoApi.todo.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import point.ttodoApi.todo.application.TodoTemplateService;

import java.util.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "태그(Tag) 관리", description = "할 일에 사용되는 태그를 관리하는 API입니다. 태그는 할 일을 분류하고 검색하는 데 사용되며, 사용자별로 고유한 태그 목록을 관리합니다.")
public class TagController {

  private final TodoTemplateService todoTemplateService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasPermission(null, 'Tag', 'READ')")
  @Operation(
          summary = "사용자 태그 목록 조회",
          description = "사용자가 할 일에 사용한 모든 태그를 중복 없이 조회합니다. 카테고리별로 필터링이 가능하며, 페이지네이션과 정렬을 지원합니다. 태그는 알파벳 순으로 정렬됩니다.",
          responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "200",
                  description = "태그 목록 조회 성공",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = Page.class),
                          examples = @ExampleObject(
                                  name = "응답 예시",
                                  value = """
                                          {
                                            "content": ["영어", "학습", "운동", "등산", "광교산", "손톱", "말랑"],
                                            "pageable": {
                                              "pageNumber": 0,
                                              "pageSize": 10,
                                              "sort": {
                                                "empty": false,
                                                "sorted": true,
                                                "unsorted": false
                                              }
                                            },
                                            "totalElements": 7,
                                            "totalPages": 1,
                                            "first": true,
                                            "last": true,
                                            "size": 10,
                                            "numberOfElements": 7,
                                            "empty": false
                                          }
                                          """
                          )
                  )
          )
  )
  public Page<String> getTags(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @RequestParam(required = false)
          @Parameter(description = "카테고리 ID 목록 (중복 허용)", example = "550e8400-e29b-41d4-a716-446655440000,550e8400-e29b-41d4-a716-446655440001")
          List<UUID> categoryIds,
          @RequestParam(defaultValue = "0")
          @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          int page,
          @RequestParam(defaultValue = "10")
          @Parameter(description = "페이지 크기", example = "10")
          int size,
          @RequestParam(defaultValue = "asc")
          @Parameter(description = "정렬 방향 (asc: 오름차순, desc: 내림차순)", example = "asc")
          String direction) {

    Sort sort = direction.equalsIgnoreCase("desc") ?
            Sort.by("tag").descending() : Sort.by("tag").ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    return todoTemplateService.getTags(UUID.fromString(user.getUsername()), categoryIds, pageable);
  }
}
