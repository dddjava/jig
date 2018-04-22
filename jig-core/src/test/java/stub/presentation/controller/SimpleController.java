package stub.presentation.controller;

import org.springframework.stereotype.Controller;
import stub.application.service.SimpleService;

@Controller
public class SimpleController {

    SimpleService service;

    public String getService() {
        service.コントローラーから呼ばれる();
        return "dummy";
    }
}
