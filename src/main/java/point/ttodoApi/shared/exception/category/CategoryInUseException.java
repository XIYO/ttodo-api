package point.ttodoApi.shared.exception.category;

import point.ttodoApi.shared.error.*;

public class CategoryInUseException extends BaseException {

  public CategoryInUseException(String categoryName, int todoCount) {
    super(ErrorCode.CATEGORY_IN_USE,
            String.format("카테고리 '%s'은(는) %d개의 할일에서 사용중이므로 삭제할 수 없습니다.",
                    categoryName, todoCount));
  }

  public CategoryInUseException() {
    super(ErrorCode.CATEGORY_IN_USE);
  }
}