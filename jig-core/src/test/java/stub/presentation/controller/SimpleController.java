package stub.presentation.controller;

import org.dddjava.jig.annotation.incubation.Progress;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import stub.application.service.SimpleService;

@Controller
@RequestMapping("simple-class")
@Progress("SimpleControllerのクラスに付けた進捗")
public class SimpleController {

    SimpleService service;

    @RequestMapping("/simple-method")
    @Progress("SimpleController#getServiceの進捗")
    public String getService() {
        service.コントローラーから呼ばれる();
        return "dummy";
    }
}
