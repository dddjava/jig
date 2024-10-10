package stub.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Operation(summary = "さまり")
    @RequestMapping("swagger-operation-annotated")
    public void swaggerOperationAnnotated() {
    }

    @Operation
    @RequestMapping("swagger-operation-annotated-none-summary")
    public void swaggerOperationAnnotatedNoneSummary() {
    }
}
