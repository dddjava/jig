package jig.classlist.infrastructure.writer;

import jig.classlist.ReportWriter;
import jig.classlist.report.Report;
import jig.classlist.report.ReportRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.util.stream.Collectors.joining;

public class TsvWriter implements ReportWriter {

    private static final Logger logger = Logger.getLogger(TsvWriter.class.getName());

    @Override
    public void writeTo(Report report, Path output) {
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writeTsvRow(writer, report.headerRow());

            for (ReportRow row : report.rows()) {
                writeTsvRow(writer, row);
            }

            logger.info(output.toAbsolutePath() + "を出力しました。");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeTsvRow(BufferedWriter writer, ReportRow reportRow) throws IOException {
        writer.write(reportRow.list().stream().collect(joining("\t")));
        writer.newLine();
    }
}
