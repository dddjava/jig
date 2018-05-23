package stub.presentation.controller;

import org.springframework.web.bind.annotation.RestController;
import stub.application.service.SimpleService;

@RestController
public class SimpleRestController {

    SimpleService service;

    public String getService() {
        service.RESTコントローラーから呼ばれる();
        return "dummy";
    }
}
