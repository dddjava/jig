package jig.presentation.controller;

import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
public class ImageController {

    @GetMapping("image")
    public ResponseEntity<byte[]> image() {
        try (ByteArrayOutputStream image = new ByteArrayOutputStream()) {
            String source = "@startuml\n" +
                    "hoge ..> fuga\n" +
                    "@enduml\n";

            SourceStringReader reader = new SourceStringReader(source);
            String desc = reader.generateImage(image);

            if (desc == null) {
                throw new IllegalArgumentException(source);
            }

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentLength(image.size())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(image.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
