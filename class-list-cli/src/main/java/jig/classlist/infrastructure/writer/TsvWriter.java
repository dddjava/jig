package jig.classlist.infrastructure.writer;

import jig.classlist.ReportWriter;
import jig.domain.model.report.ReportRow;
import jig.domain.model.report.Reports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.stream.Collectors.joining;

public class TsvWriter implements ReportWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TsvWriter.class);

    @Override
    public void writeTo(Reports reports, Path outputBasePath) {
        reports.each(report -> {
            Path output = outputBasePath.toAbsolutePath().getParent().resolve(report.title().value() + "_" + outputBasePath.getFileName());
            try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
                writeTsvRow(writer, report.headerRow());

                for (ReportRow row : report.rows()) {
                    writeTsvRow(writer, row);
                }

                LOGGER.info(output.toAbsolutePath() + "を出力しました。");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void writeTsvRow(BufferedWriter writer, ReportRow reportRow) throws IOException {
        writer.write(reportRow.list().stream().collect(joining("\t")));
        writer.newLine();
    }
}
