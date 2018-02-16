package jig.infrastructure;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramRepository;
import jig.domain.model.DiagramSource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DiagramRepositoryImply implements DiagramRepository {

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
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public DiagramSource getSource(DiagramIdentifier identifier) {
        return map.get(identifier);
    }
}
