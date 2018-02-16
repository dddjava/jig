package jig.infrastructure;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramRepository;
import jig.domain.model.DiagramSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DiagramRepositoryImpl implements DiagramRepository {

    private Map<DiagramIdentifier, DiagramSource> map = new ConcurrentHashMap<>();
    private Set<Diagram> diagrams = ConcurrentHashMap.newKeySet();

    @Override
    public DiagramIdentifier register(DiagramSource source) {
        DiagramIdentifier identifier = source.getIdentifier();
        map.put(identifier, source);
        return identifier;
    }

    @Override
    public void register(Diagram diagram) {
        diagrams.add(diagram);
    }

    @Override
    public Diagram get(DiagramIdentifier identifier) {
        return diagrams.stream()
                .filter(diagram -> diagram.matches(identifier))
                .findFirst()
                .orElseGet(() -> {
                    try (InputStream inputStream = new ClassPathResource("notfound.png").getInputStream()) {
                        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
                        return new Diagram(DiagramIdentifier.notFound(), bytes);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    @Override
    public DiagramSource getSource(DiagramIdentifier identifier) {
        return map.get(identifier);
    }
}
