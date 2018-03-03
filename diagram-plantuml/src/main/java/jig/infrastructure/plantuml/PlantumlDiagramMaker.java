package jig.infrastructure.plantuml;

import jig.domain.model.diagram.Diagram;
import jig.domain.model.diagram.DiagramMaker;
import jig.domain.model.diagram.DiagramSource;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class PlantumlDiagramMaker implements DiagramMaker {

    public PlantumlDiagramMaker() {
        List<String> testDotStrings = GraphvizUtils.getTestDotStrings(false);
        if (!testDotStrings.contains("Installation seems OK. File generation OK")) {
            throw new IllegalStateException("ダイアグラム出力に必要な環境が満たされていません。" + testDotStrings);
        }
    }

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
