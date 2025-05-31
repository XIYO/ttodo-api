package point.zzicback.common.validation.fieldcompare;

import java.lang.annotation.*;

/**
 * 검증 결과를 나타낼 필드를 표시하는 어노테이션
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface CompareResult {
}
