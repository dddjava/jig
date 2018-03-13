package jig.classlist;

import jig.domain.model.list.ConverterCondition;
import jig.domain.model.list.kind.ModelKind;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameDictionary;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ExcelWriter {

    private static final Logger logger = Logger.getLogger(ExcelWriter.class.getName());

    @Value("${output.list.type}")
    String modelKind;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameDictionary japaneseNameRepository;

    public void writeTo(Path output) {
        ModelKind modelKind = ModelKind.valueOf(this.modelKind.toUpperCase());

        try (Workbook book = new XSSFWorkbook();
             OutputStream os = Files.newOutputStream(output)) {
            Sheet sheet = book.createSheet();
            writeRow(modelKind.headerLabel(), sheet.createRow(0));

            Relations methods = relationRepository.allMethods();
            for (Relation methodRelation : methods.list()) {
                if (modelKind.correct(methodRelation.from())) {
                    ConverterCondition condition = new ConverterCondition(methodRelation, relationRepository, japaneseNameRepository);
                    writeRow(modelKind.row(condition), sheet.createRow(sheet.getLastRowNum() + 1));
                }
            }

            book.write(os);
            logger.info(output.toAbsolutePath() + "を出力しました。");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeRow(List<String> list, Row row) {
        list.forEach(item -> {
            short lastCellNum = row.getLastCellNum();
            Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);
            cell.setCellValue(item);
        });
    }
}
