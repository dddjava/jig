package jig.classlist;

import jig.domain.model.list.MethodRelationNavigator;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.joining;

@Component
public class TsvWriter extends AbstractListWriter {

    private static final Logger logger = Logger.getLogger(TsvWriter.class.getName());

    public void writeTo(Path output) {

        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writeTsvRow(writer, modelKind.headerLabel());

            for (MethodRelationNavigator condition : list()) {
                writeTsvRow(writer, modelKind.row(condition));
            }

            logger.info(output.toAbsolutePath() + "を出力しました。");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeTsvRow(BufferedWriter writer, List<String> list) throws IOException {
        writer.write(list.stream().collect(joining("\t")));
        writer.newLine();
    }
}
