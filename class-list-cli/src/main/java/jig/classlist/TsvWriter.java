package jig.classlist;

import jig.domain.model.list.ConverterCondition;
import jig.domain.model.list.kind.ModelKind;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class TsvWriter {

    private static final Logger logger = Logger.getLogger(TsvWriter.class.getName());

    @Value("${output.list.type}")
    String modelKind;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameDictionary japaneseNameRepository;

    public void writeTo(Path output) {
        ModelKind modelKind = ModelKind.valueOf(this.modelKind.toUpperCase());

        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writeTsvRow(writer, modelKind.headerLabel());

            Relations methods = relationRepository.allMethods();
            for (Relation methodRelation : methods.list()) {
                if (modelKind.correct(methodRelation.from())) {
                    ConverterCondition condition = new ConverterCondition(methodRelation, relationRepository, japaneseNameRepository);
                    writeTsvRow(writer, modelKind.row(condition));
                }
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
