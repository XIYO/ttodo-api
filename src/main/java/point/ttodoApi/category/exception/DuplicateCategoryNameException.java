package point.ttodoApi.category.exception;

import point.ttodoApi.shared.error.*;

public class DuplicateCategoryNameException extends DuplicateResourceException {

  public DuplicateCategoryNameException(String categoryName) {
    super(ErrorCode.DUPLICATE_CATEGORY_NAME,
            String.format("카테고리명 '%s'은(는) 이미 사용중입니다.", categoryName));
  }

  public DuplicateCategoryNameException() {
    super(ErrorCode.DUPLICATE_CATEGORY_NAME);
  }
}