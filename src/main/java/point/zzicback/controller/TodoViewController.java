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
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoViewController {

    private final TodoService todoService;

    @GetMapping
    public String viewTodos(Model model) {
        model.addAttribute("todos", todoService.getTodoList(false));
        model.addAttribute("doneTodos", todoService.getTodoList(true));
        return "todos";
    }

    @PostMapping("/done-toggle")
    public String done(@RequestParam Long id, @RequestParam Boolean done, @RequestHeader("Referer") String referer) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setDone(!done);
        todoService.updateTodo(todo);
        return "redirect:" + referer;
    }

    @GetMapping("/{id}")
    public String todoDetail(Model model, @PathVariable Long id) {
        model.addAttribute("todo", todoService.getTodoById(id));
        return "todo-detail";
    }

    @PostMapping
    public String add(CreateTodoRequest request) {
        Todo todo = request.ToEntity();
        todoService.createTodo(todo);
        return "redirect:/todos";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        todoService.deleteTodo(id);
        return "redirect:/todos";
    }

    @PostMapping("/update/{id}")
    public String update(UpdateTodoRequest request, @PathVariable Long id, @RequestParam String referer) {
        Todo todo = request.ToEntity();
        todo.setId(id);
        todoService.updateTodo(todo);
        return "redirect:" + referer;
    }

    @GetMapping("/update/{id}")
    public String updateDetail(Model model, @PathVariable Long id, @RequestHeader("Referer") String referer) {
        model.addAttribute("todo", todoService.getTodoById(id));
        model.addAttribute("referer", referer);
        return "todo-update";
    }
}
