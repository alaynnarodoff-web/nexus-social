package clarity.internship.backend_api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
public class HelloWorldController {
    @GetMapping("/hello/{name}")
    public String helloWorld(@PathVariable("name") String name) {
        return "Hello, " + name;
    }
}
