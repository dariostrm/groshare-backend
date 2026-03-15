package com.jakobdario.groshare;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/api/hello")
    public String helloWorld() {
        return "Hello World! GroShare API is successfully running";
    }
}
