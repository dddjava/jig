package jig.presentation.controller;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramMaker;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ImageController {

    @Autowired
    DiagramMaker maker;

    @GetMapping("image")
    public ResponseEntity<byte[]> image() {
        String source = "@startuml\n" +
                "hoge ..> fuga\n" +
                "@enduml\n";

        return image(source);
    }

    @PostMapping(value = "image", params = "source")
    public ResponseEntity<byte[]> submit(@RequestParam("source") String source) {
        return image("@startuml\n" + source + "\n@enduml");
    }

    @PostMapping("image")
    public ResponseEntity<byte[]> image(@RequestBody String source) {
        DiagramSource diagramSource = new DiagramSource(source);
        DiagramIdentifier identifier = maker.request(diagramSource);
        maker.make(identifier);
        Diagram diagram = maker.get(identifier);
        byte[] bytes = diagram.getBytes();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentLength(bytes.length)
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }
}
