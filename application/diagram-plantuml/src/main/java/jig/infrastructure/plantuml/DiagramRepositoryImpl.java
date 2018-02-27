package jig.infrastructure.plantuml;

import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramIdentifier;
import jig.domain.model.diagram.DiagramRepository;
import jig.domain.model.diagram.DiagramSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DiagramRepositoryImpl implements DiagramRepository {

    private Map<DiagramIdentifier, DiagramSource> map = new ConcurrentHashMap<>();
    private Map<DiagramIdentifier, Diagram> diagrams = new ConcurrentHashMap<>();

    @Override
    public DiagramIdentifier registerSource(DiagramSource source) {
        DiagramIdentifier identifier = getDiagramIdentifier(source);
        map.put(identifier, source);
        return identifier;
    }

    private DiagramIdentifier getDiagramIdentifier(DiagramSource source) {
        try {
            String value = source.value();
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(value.getBytes("UTF-8"), 0, value.length());
            String sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
            return new DiagramIdentifier(sha1);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void register(DiagramIdentifier identifier, Diagram diagram) {
        diagrams.put(identifier, diagram);
    }

    @Override
    public Diagram get(DiagramIdentifier identifier) {
        return diagrams.entrySet().stream()
                .filter(entry -> entry.getKey().equals(identifier))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> {
                    try (InputStream inputStream = new ClassPathResource("notfound.png").getInputStream()) {
                        byte[] bytes = StreamUtils.copyToByteArray(inputStream);
                        return new Diagram(bytes);
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
