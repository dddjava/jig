package stub.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import stub.application.service.SimpleService;

@Controller
@RequestMapping("simple-class")
public class SimpleController {

    SimpleService service;

    @RequestMapping("/simple-method")
    public String getService() {
        service.コントローラーから呼ばれる();
        return "dummy";
    }
}
