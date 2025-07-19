package point.ttodoApi.category.exception;

import point.ttodoApi.common.error.*;

import java.util.UUID;

public class CategoryNotFoundException extends DataNotFoundException {
    
    public CategoryNotFoundException(UUID categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND, 
              String.format("카테고리(ID: %s)를 찾을 수 없습니다.", categoryId));
    }
    
    public CategoryNotFoundException(Long categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND, 
              String.format("카테고리(ID: %d)를 찾을 수 없습니다.", categoryId));
    }
    
    public CategoryNotFoundException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}