package jig.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TemporaryFolder.class)
public class ClassListApplicationTest {

    @Test
    public void testService(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();

        Path sutPath = Paths.get("..", "sut").toAbsolutePath();
        ClassListApplication.main(new String[]{
                "--target.class=" + sutPath.resolve("build/classes/java/main"),
                "--target.source=" + sutPath.resolve("src/main/java"),
                "--output.list.type=" + "service",
                "--output.list.name=" + output.toString()
        });

        assertThat(Files.readAllLines(output.toPath()))
                .hasSize(3)
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.application.service.CanonicalService",
                        "sut.application.service.CanonicalService")
                .doesNotContain(
                        "sut.application.ThrowsUnknownExceptionService");
    }

    @Test
    public void testRepository(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();

        Path sutPath = Paths.get("..", "sut").toAbsolutePath();
        ClassListApplication.main(new String[]{
                "--target.class=" + sutPath.resolve("build/classes/java/main"),
                "--target.source=" + sutPath.resolve("src/main/java"),
                "--output.list.type=" + "repository",
                "--output.list.name=" + output.toString()
        });

        assertThat(Files.readAllLines(output.toPath()))
                .hasSize(3)
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.domain.model.hoge.HogeRepository",
                        "sut.domain.model.fuga.FugaRepository");
    }
}