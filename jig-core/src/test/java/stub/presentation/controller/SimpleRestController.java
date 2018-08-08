package stub.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import stub.application.service.SimpleService;

@RestController
public class SimpleRestController {

    SimpleService service;

    @GetMapping(value = "test-get")
    @PostMapping(path = "test-post")
    public String getService() {
        service.RESTコントローラーから呼ばれる();
        return "dummy";
    }
}
