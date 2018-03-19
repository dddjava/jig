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
        runApplication(output, "service");

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
        runApplication(output, "repository");

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
                        "all()", "Hoges", "HogeDatasource.all()",
                        "[s.i.d.H.findOne()]", "HOGE");
        assertThat(Files.readAllLines(output.toPath()).get(1))
                .containsSubsequence("FugaRepository", "",
                        "get", "Fuga", "FugaDatasource",
                        "A.insert", "F.get", "sut.piyo");
    }

    @Test
    public void identifier(Path temporaryFolder) throws Exception {
        File output = temporaryFolder.resolve("output.tsv").toFile();
        runApplication(output, "identifier");

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
        runApplication(output, "enum");

        assertThat(Files.readAllLines(output.toPath()))
                .extracting(value -> value.split("\t")[0])
                .containsExactlyInAnyOrder(
                        "クラス名",
                        "sut.domain.model.kind.SimpleEnum",
                        "sut.domain.model.kind.BehaviourEnum",
                        "sut.domain.model.kind.ParameterizedEnum",
                        "sut.domain.model.kind.PolymorphismEnum");
    }

    private void runApplication(File output, String listType) {
        ClassListApplication.main(new String[]{
                "--project.path=" + Paths.get("..", "sut").toAbsolutePath(),
                "--output.list.type=" + listType,
                "--output.list.name=" + output.toString()
        });
    }
}