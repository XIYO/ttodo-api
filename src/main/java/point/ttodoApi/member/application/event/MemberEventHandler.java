package point.ttodoApi.member.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import point.ttodoApi.category.application.CategoryService;
import point.ttodoApi.category.application.command.CreateCategoryCommand;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberEventHandler {
  private final CategoryService categoryService;

  @EventListener
  public void handleMemberCreated(MemberCreatedEvent event) {
    List<String> categoryNames = List.of("기본", "개인", "업무");
    categoryNames.forEach(name -> {
      CreateCategoryCommand command = new CreateCategoryCommand(
              event.memberId(), name, null, null);
      categoryService.createCategory(command);
    });
  }
}
