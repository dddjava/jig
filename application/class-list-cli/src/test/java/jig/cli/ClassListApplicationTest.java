package jig.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassListApplicationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void test() throws Exception {
        Path sutPath = Paths.get("..", "sut").toAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder("./gradlew", ":sut:build");
        pb.directory(sutPath.getParent().toFile());
        Process process = pb.start();
        int result = process.waitFor();
        assertThat(result).isEqualTo(0);

        File output = new File(temporaryFolder.getRoot(), "output.tsv");

        ClassListApplication.main(new String[]{
                "--target.class=" + sutPath.resolve("build/classes/java/main"),
                "--target.source=" + sutPath.resolve("src/main/java"),
                "--output.list.name=" + output.toString()
        });

        assertThat(Files.readAllLines(output.toPath()))
                .hasSize(4)
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.application.CanonicalService",
                        "sut.application.CanonicalService",
                        "sut.application.ThrowsUnknownExceptionService");
    }
}