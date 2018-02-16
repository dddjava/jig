package jig.infrastructure;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramMaker;
import jig.domain.model.DiagramSource;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DiagramMakerImpl implements DiagramMaker {

    private Map<DiagramIdentifier, DiagramSource> map = new ConcurrentHashMap<>();
    private Map<DiagramIdentifier, Diagram> diagrams = new ConcurrentHashMap<>();

    @Override
    public DiagramIdentifier request(DiagramSource source) {
        DiagramIdentifier identifier = new DiagramIdentifier();
        map.put(identifier, source);
        return identifier;
    }

    @Override
    public void make(DiagramIdentifier identifier) {
        DiagramSource source = map.get(identifier);
        String source1 = source.value();
        try (ByteArrayOutputStream image = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(source1);
            String desc = reader.generateImage(image);

            if (desc == null) {
                throw new IllegalArgumentException(source1);
            }
            Diagram diagram = new Diagram(image.toByteArray());
            diagrams.put(identifier, diagram);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Diagram get(DiagramIdentifier identifier) {
        return diagrams.get(identifier);
    }
}
