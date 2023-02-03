package com.example.springbootazsqlaks;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
public class Controller{

    private final TodoRepository todoRepository;

    public Controller(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
    @GetMapping("/test")
    public String test(){
        return "test";
    }

    @PostMapping("/addtodo")
    @ResponseStatus(HttpStatus.CREATED)
    public Todo createTodo(@RequestBody Todo todo) {
        return todoRepository.save(todo);
    }

    @GetMapping("/gettodo")
    public Iterable<Todo> getTodos() {
        return todoRepository.findAll();
    }

 

}