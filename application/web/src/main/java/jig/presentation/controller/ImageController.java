package jig.presentation.controller;

import jig.application.service.DiagramService;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import jig.presentation.view.DiagramView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("image")
public class ImageController {

    @Autowired
    DiagramService service;

    @PostMapping
    public DiagramIdentifier request(@RequestBody String source) {
        DiagramIdentifier identifier = service.request(new DiagramSource(source));
        service.generateAsync(identifier);
        return identifier;
    }

    @GetMapping("{identifier}")
    public DiagramView get(@PathVariable("identifier") DiagramIdentifier identifier) {
        return new DiagramView(service.get(identifier));
    }
}
