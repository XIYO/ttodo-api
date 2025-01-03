package point.zzicback.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import point.zzicback.dto.request.CreateTodoRequest;
import point.zzicback.dto.request.UpdateTodoRequest;
import point.zzicback.model.Todo;
import point.zzicback.service.TodoService;

@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // 메인 페이지에서 할 일 목록을 조회
    @GetMapping
    public String viewTodos(Model model) {
        model.addAttribute("todos", todoService.getAll());
        return "todos"; // Thymeleaf 템플릿 이름
    }

    @GetMapping("js")
    public String viewTodosJs() {
        return "todos-js"; // Thymeleaf 템플릿 이름
    }

    // 새 할 일을 추가
    @PostMapping
    public String add(CreateTodoRequest createTodoRequest) {
        Todo todo = createTodoRequest.toEntity();
        todoService.add(todo);
        return "redirect:/todos"; // 추가 후 메인 페이지로 리다이렉트
    }

    // 할 일을 수정
    @PostMapping("/{id}/delete")
    public String doneToggle(@PathVariable Long id, @RequestParam Boolean done) {
        Todo todo = todoService.getById(id);
        todo.setDone(done);
        todoService.modify(todo);
        return "redirect:/todos"; // 완료 후 메인 페이지로 리다이렉트
    }

    // 할 일을 수정
    @PatchMapping("/{id}")
    public String doneToggle2(@PathVariable Long id, @RequestParam Boolean done) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setDone(!done);
        todoService.modify(todo);
        return "redirect:/todos"; // 미완료 후 메인 페이지로 리다이렉트
    }

    // 할 일 상세 조회
    @GetMapping("/{id}")
    public String viewTodoDetail(@PathVariable Long id, Model model) {
        Todo todo = todoService.getById(id);
        if (todo != null) {
            model.addAttribute("todo", todo);
        }
        return "todo-detail"; // Thymeleaf 디테일 템플릿 이름
    }

    // 할 일을 삭제
    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        todoService.remove(id);
        return "redirect:/todos"; // 삭제 후 메인 페이지로 리다이렉트
    }

    // 할 일 삭제
    @DeleteMapping("/{id}")
    public String deleteTodo(@PathVariable Long id) {
        todoService.remove(id);
        return "redirect:/todos"; // 삭제 후 메인 페이지로 리다이렉트
    }

}
