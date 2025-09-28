package point.ttodoApi.shared.validation;

import jakarta.validation.*;
import lombok.experimental.UtilityClass;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.ErrorCode;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * TTODO 검증 유틸리티
 * 도메인 전반에 걸친 일관된 검증 로직 제공
 */
@UtilityClass
public class TtodoValidationUtils {
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    /**
     * 객체 검증 및 예외 발생
     * 
     * @param object 검증할 객체
     * @param groups 검증 그룹
     * @throws BusinessException 검증 실패 시
     */
    public static void validateAndThrow(Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object, groups);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, errorMessage);
        }
    }
    
    /**
     * Command 객체 검증 (생성)
     */
    public static void validateCreateCommand(Object command) {
        validateAndThrow(command, TtodoValidationGroups.Create.class);
    }
    
    /**
     * Command 객체 검증 (수정)
     */
    public static void validateUpdateCommand(Object command) {
        validateAndThrow(command, TtodoValidationGroups.Update.class);
    }
    
    /**
     * Query 객체 검증
     */
    public static void validateQuery(Object query) {
        validateAndThrow(query, TtodoValidationGroups.Query.class);
    }
    
    /**
     * 인증 객체 검증
     */
    public static void validateAuth(Object authObject) {
        validateAndThrow(authObject, TtodoValidationGroups.Auth.class);
    }
    
    /**
     * 비즈니스 로직 검증
     */
    public static void validateBusiness(Object businessObject) {
        validateAndThrow(businessObject, TtodoValidationGroups.Business.class);
    }
    
    /**
     * 검증 결과 반환 (예외 발생하지 않음)
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
        return validator.validate(object, groups);
    }
    
    /**
     * 검증 성공 여부 확인
     */
    public static boolean isValid(Object object, Class<?>... groups) {
        return validator.validate(object, groups).isEmpty();
    }
}