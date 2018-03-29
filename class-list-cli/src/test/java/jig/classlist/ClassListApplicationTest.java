package jig.classlist;

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
        runApplication(output);

        output = new File(output.getParentFile(), "SERVICE_output.tsv");
        assertThat(Files.readAllLines(output.toPath()))
                .hasSize(5)
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.application.service.CanonicalService",
                        "sut.application.service.CanonicalService",
                        "sut.application.service.ThrowsUnknownExceptionService",
                        "sut.application.service.ThrowsUnknownExceptionService");
        assertThat(Files.readAllLines(output.toPath()).get(2))
                .containsSubsequence("CanonicalService", "典型的なサービス", "fuga(FugaIdentifier)", "Fuga", "FugaRepository");
    }

    @Test
    public void testRepository(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();
        runApplication(output);

        output = new File(output.getParentFile(), "REPOSITORY_output.tsv");
        assertThat(Files.readAllLines(output.toPath()))
                .hasSize(4)
                .extracting(value -> value.split("\t")[0])
                .containsExactly(
                        "クラス名",
                        "sut.domain.model.fuga.FugaRepository",
                        "sut.domain.model.fuga.FugaRepository",
                        "sut.domain.model.hoge.HogeRepository");

        assertThat(Files.readAllLines(output.toPath()).get(3))
                .containsSubsequence("HogeRepository", "ほげリポジトリ",
                        "all()", "Hoges",
                        "HOGE");
        assertThat(Files.readAllLines(output.toPath()).get(1))
                .containsSubsequence("FugaRepository", "",
                        "get", "Fuga",
                        "sut.piyo");
    }

    @Test
    public void identifier(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();
        runApplication(output);

        output = new File(output.getParentFile(), "IDENTIFIER_output.tsv");
        assertThat(Files.readAllLines(output.toPath()))
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.domain.model.fuga.FugaIdentifier",
                        "sut.domain.model.fuga.FugaName");
    }

    @Test
    public void enums(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();
        runApplication(output);

        output = new File(output.getParentFile(), "ENUM_output.tsv");
        assertThat(Files.readAllLines(output.toPath()))
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.domain.model.kind.SimpleEnum",
                        "sut.domain.model.kind.BehaviourEnum",
                        "sut.domain.model.kind.ParameterizedEnum",
                        "sut.domain.model.kind.PolymorphismEnum");
    }

    private void runApplication(File output) {
        ClassListApplication.main(new String[]{
                "--project.path=" + Paths.get("..", "sut").toAbsolutePath(),
                "--output.omit.prefix=xxx",
                "--output.list.name=" + output.toString()
        });
    }
}