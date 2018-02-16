package jig.presentation.controller;

import jig.application.service.DiagramService;
import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("image")
public class ImageController {

    @Autowired
    DiagramService service;

    @PostMapping(params = "source")
    public ResponseEntity<byte[]> submit(@RequestParam("source") String source) {
        DiagramSource diagramSource = new DiagramSource("@startuml\n" + source + "\n@enduml");
        Diagram diagram = service.generateImmediately(diagramSource);
        return response(diagram);
    }

    private ResponseEntity<byte[]> response(Diagram diagram) {
        byte[] bytes = diagram.getBytes();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentLength(bytes.length)
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @PostMapping
    public DiagramIdentifier request(@RequestBody String source) {
        return service.request(new DiagramSource(source));
    }

    @GetMapping("{identifier}")
    public ResponseEntity<byte[]> get(@PathVariable("identifier") DiagramIdentifier identifier) {
        return response(service.get(identifier));
    }
}
