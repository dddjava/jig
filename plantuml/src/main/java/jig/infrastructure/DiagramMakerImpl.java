package jig.infrastructure;

import jig.domain.model.*;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class DiagramMakerImpl implements DiagramMaker {

    @Autowired
    DiagramRepository repository;

    @Override
    public void make(DiagramIdentifier identifier) {
        DiagramSource source = repository.getSource(identifier);
        String source1 = source.value();
        try (ByteArrayOutputStream image = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(source1);
            String desc = reader.generateImage(image);

            if (desc == null) {
                throw new IllegalArgumentException(source1);
            }
            repository.register(new Diagram(identifier, image.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Async
    @Override
    public void makeAsync(DiagramIdentifier identifier) {
        make(identifier);
    }
}
