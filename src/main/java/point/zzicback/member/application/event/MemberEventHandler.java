package point.zzicback.member.application.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import point.zzicback.category.application.CategoryService;
import point.zzicback.category.application.command.CreateCategoryCommand;

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
