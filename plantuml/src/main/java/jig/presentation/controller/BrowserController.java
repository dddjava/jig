package jig.presentation.controller;

import jig.application.service.DiagramService;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("browser")
public class BrowserController {

    @Autowired
    DiagramService service;

    @PostMapping
    public String submit(@RequestParam("source") String source) {
        DiagramSource diagramSource = new DiagramSource("@startuml\n" + source + "\n@enduml");
        DiagramIdentifier identifier = service.generateImmediately(diagramSource);
        return "redirect:/image/" + identifier.getIdentifier();
    }

    @GetMapping
    public String identifier(@RequestParam("identifier") String identifier) {
        return "redirect:/image/" + identifier;
    }
}
