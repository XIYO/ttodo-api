# Removed Tooling Archive

이 문서는 빌드 간소화 과정에서 제거된 개발/품질 도구를 추후 필요 시 신속 복구하기 위한 스니펫을 정리합니다.

> NOTE: 현재 프로젝트는 핵심 Spring Boot + MapStruct + Testcontainers 최소 구성만 유지합니다.

## 1. SonarQube

```gradle
plugins { id 'org.sonarqube' version '6.3.1.5724' }

sonarqube {
  properties {
    property 'sonar.projectKey', 'ttodo-api'
    property 'sonar.projectName', 'TTODO API'
    property 'sonar.sources', 'src/main/java'
    property 'sonar.tests', 'src/test/java'
    property 'sonar.java.binaries', 'build/classes/java/main'
    property 'sonar.coverage.jacoco.xmlReportPaths', 'build/reports/jacoco/test/jacocoTestReport.xml'
  }
}
```

## 2. Jacoco

```gradle
plugins { id 'jacoco' }

jacoco { toolVersion = '0.8.12' }

tasks.test { finalizedBy tasks.jacocoTestReport }

jacocoTestReport {
  reports { xml.required = true; html.required = true }
}
```

## 3. OpenRewrite

```gradle
plugins { id 'org.openrewrite.rewrite' version '6.27.0' }

dependencies {
  rewrite platform("org.openrewrite.recipe:rewrite-recipe-bom:2.22.0")
  rewrite("org.openrewrite.recipe:rewrite-spring")
  rewrite("org.openrewrite.recipe:rewrite-migrate-java")
}

rewrite {
  activeRecipe("point.ttodoApi.SimpleMemberToUserRefactoring")
  // configFile = file("rewrite-simple.yml")
}
```

## 4. Dependency Updates Plugin

```gradle
plugins { id 'com.github.ben-manes.versions' version '0.52.0' }
// 실행: ./gradlew dependencyUpdates
```

## 5. Null Safety 자동 package-info 생성 태스크

```gradle
task ensureNullMarked {
  doLast {
    def srcDir = file('src/main/java')
    fileTree(srcDir) { include '**/*.java'; exclude '**/package-info.java' }
      .collect { it.parentFile }.unique().each { dir ->
        def f = new File(dir, 'package-info.java')
        if(!f.exists()) {
          def pkg = dir.path.replace(srcDir.path + File.separator,'').replace(File.separator,'.')
          f.text = """@org.jspecify.annotations.NullMarked
package ${pkg};
"""
        }
      }
  }
}
compileJava.dependsOn ensureNullMarked
```

## 6. Legacy refactoring.gradle 적용

```gradle
apply from: 'gradle/refactoring.gradle'
```

## 복구 절차 권장 순서

1. 품질/커버리지 필요 → Jacoco → SonarQube 순으로 추가
2. 코드 리팩토링 대량 적용 필요 시 OpenRewrite 추가
3. 의존성 점검 필요 시 versions 플러그인 임시 적용 후 제거
4. Null 정책 자동화 재도입 필요 시 ensureNullMarked 태스크 복원

## 재도입 시 유의 사항

- Sonar 재도입 시 Jacoco XML 보고서가 필요하므로 Jacoco 먼저 추가
- OpenRewrite recipe 활성화 전 Dry Run(`./gradlew rewriteDryRun`) 권장
- ensureNullMarked 태스크는 Git diff 노이즈를 만들 수 있음 → 필요 패키지만 수동 유지 권장
