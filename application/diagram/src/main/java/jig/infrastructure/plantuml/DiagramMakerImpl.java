package jig.infrastructure.plantuml;

import jig.domain.model.Diagram;
import jig.domain.model.DiagramMaker;
import jig.domain.model.DiagramSource;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class DiagramMakerImpl implements DiagramMaker {

    @Override
    public Diagram make(DiagramSource source) {
        try (ByteArrayOutputStream image = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(source.value());
            String desc = reader.generateImage(image);

            if (desc == null) {
                throw new IllegalArgumentException(source.value());
            }
            return new Diagram(image.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
