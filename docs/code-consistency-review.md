# 코드 통일성 검토 보고서

## 1. 개요
- 분석 범위: `src/main/java/point/ttodoApi` 및 `src/test/java/point/ttodoApi` 전반의 구조, 네이밍, 코딩 스타일, 문서화, 테스트 패턴.
- 목적: 모듈 간 통일성 유지 현황을 확인하고, 개선이 필요한 영역을 제안.

## 2. 구조 및 계층 일관성
- 대부분의 모듈이 `presentation → application → domain → infrastructure` 계층 구조를 유지하며 책임이 명확하게 분리되어 있습니다. 예시로 Todo 모듈은 `TodoController`(presentation), `TodoTemplateService`(application), `CategoryRepository`(infrastructure)를 통해 흐름을 구성합니다.【F:src/main/java/point/ttodoApi/todo/presentation/TodoController.java†L1-L200】【F:src/main/java/point/ttodoApi/todo/application/TodoTemplateService.java†L1-L200】【F:src/main/java/point/ttodoApi/category/infrastructure/persistence/CategoryRepository.java†L1-L24】
- 도메인 엔티티에는 협업자 검증, 권한 확인 등 비즈니스 로직이 포함되어 있어 도메인 중심 설계 방향과 일치합니다.【F:src/main/java/point/ttodoApi/category/domain/Category.java†L16-L200】

## 3. 네이밍 및 패키지 규칙
- 모든 패키지가 `point.ttodoApi`로 시작하는데, Java 관례(소문자 패키지)와 프로젝트 가이드(소문자 패키지) 모두와 달라 일관된 소문자 네이밍(`point.ttodoapi`)으로 교정하는 편이 좋습니다.【F:src/main/java/point/ttodoApi/todo/presentation/TodoController.java†L1-L200】
- `TodoTemplateService`의 `private final UserService UserService;`처럼 필드명이 대문자로 시작하는 사례가 있어 Camel Case 규칙에서 벗어납니다. 서비스 전반에서 필드/변수의 첫 글자를 소문자로 맞추는 정비가 필요합니다.【F:src/main/java/point/ttodoApi/todo/application/TodoTemplateService.java†L29-L76】

## 4. 코딩 스타일 및 포맷팅
- `BaseEntity` 등 다수 클래스가 4칸 들여쓰기를 사용하고 있어 리포지토리 가이드라인(2칸 들여쓰기)과 어긋납니다. IDE 포맷터 설정을 공유하여 프로젝트 전반에 2칸 들여쓰기를 적용할 것을 권장합니다.【F:src/main/java/point/ttodoApi/shared/domain/BaseEntity.java†L1-L37】
- `TodoController` 등에서 `org.springframework.http.*`와 같은 와일드카드 임포트를 사용하지만 다른 클래스는 구체 임포트를 사용해 혼재되어 있습니다. 와일드카드 사용 기준을 정의하거나 전부 구체 임포트로 맞추면 가독성이 개선됩니다.【F:src/main/java/point/ttodoApi/todo/presentation/TodoController.java†L1-L200】
- `Category` 엔티티는 `@Setter` 전체 공개와 빌더 패턴을 동시에 사용하여 불변성 전략이 혼재되어 있습니다. 변경 가능성을 최소화하려면 특정 목적(setter 제거 + 명령 메서드 도입 등)을 명확히 하는 편이 좋습니다.【F:src/main/java/point/ttodoApi/category/domain/Category.java†L16-L200】

## 5. 문서화 및 API 설명
- 프레젠테이션 계층에서 Swagger 어노테이션을 적극 활용하여 상세 설명과 예시를 제공하고 있어 문서화 일관성이 좋습니다.【F:src/main/java/point/ttodoApi/todo/presentation/TodoController.java†L28-L200】
- 다만 설명 문자열이 매우 길어 컨트롤러에 집중되어 있으므로, 공통 설명을 별도 상수나 YAML 문서로 분리하는 것도 유지 보수성 측면에서 고려해볼 만합니다.

## 6. 테스트 구성
- `BaseIntegrationTest`는 Testcontainers를 활용해 PostgreSQL/Redis 환경을 공통으로 제공하며, 테스트 프로파일 설정도 일관적으로 정의되어 있습니다.【F:src/test/java/point/ttodoApi/test/BaseIntegrationTest.java†L1-L86】
- 모듈별 테스트 디렉터리가 실제 모듈 구조와 대응되어 있어 계층 간 테스트 책임이 명확합니다. 다만 단위 테스트/통합 테스트의 기준을 README 등에서 명시하면 온보딩이 쉬워집니다.

## 7. 개선 제안 요약
1. 패키지 및 필드 네이밍을 전부 소문자/카멜케이스로 정비하여 관례를 통일합니다.
2. IDE/빌드 툴 레벨에서 2칸 들여쓰기 포맷팅 규칙을 강제하여 스타일을 일관화합니다.
3. 와일드카드 임포트, Setter 공개 여부 등 클래스별 스타일 편차를 정리할 가이드 문서를 추가합니다.
4. Swagger 설명은 반복되는 부분을 분리하여 유지 보수성을 높입니다.

위 사안 외에는 계층 구조와 테스트 구성 등 핵심 구조는 일관되게 유지되고 있어 안정적인 서비스 확장에 유리한 상태입니다.
