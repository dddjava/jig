package jig.application.usecase;

import jig.application.service.DatasourceService;
import jig.application.service.SpecificationService;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.japanese.TypeJapaneseName;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.type.TypePerspective;
import jig.infrastructure.JigPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import stub.application.service.CanonicalService;
import stub.domain.model.type.fuga.FugaRepository;
import testing.TestConfiguration;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class ReportServiceTest {

    @Autowired
    ReportService sut;

    @Autowired
    SpecificationService specificationService;
    @Autowired
    DatasourceService datasourceService;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @Test
    void クラスを読み込むE2Eに近いテスト() throws Exception {
        // 読み込む対象のソースを取得
        Path path = Paths.get(TestSupport.defaultPackageClassURI());
        JigPaths jigPaths = new JigPaths(
                path.toString(),
                path.toString(),
                // Mapper.xmlのためだが、ここではHitしなくてもテストのクラスパスから読めてしまう
                "not/read/resources",
                // TODO ソースディレクトリの安定した取得方法が欲しい
                "not/read/sources");

        specificationService.importSpecification(jigPaths.getSpecificationSources());
        datasourceService.importDatabaseAccess(jigPaths.getSqlSources());

        japaneseNameRepository.register(new TypeJapaneseName(new TypeIdentifier(CanonicalService.class), new JapaneseName("暫定和名1")));
        assertThat(sut.methodReportOn(MethodPerspective.SERVICE).rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.application.service.CanonicalService, 暫定和名1, fuga(FugaIdentifier), Fuga, [HogeRepository, FugaRepository], [HogeRepository.method(), FugaRepository.get(FugaIdentifier)]]",
                        "[stub.application.service.CanonicalService, 暫定和名1, method(), void, [], []]",
                        "[stub.application.service.SimpleService, , コントローラーから呼ばれない(), void, [], []]",
                        "[stub.application.service.SimpleService, , コントローラーから呼ばれる(), void, [], []]"
                );

        japaneseNameRepository.register(new TypeJapaneseName(new TypeIdentifier(FugaRepository.class), new JapaneseName("暫定和名2")));
        assertThat(sut.methodReportOn(MethodPerspective.REPOSITORY).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsSequence(
                        "[stub.domain.model.type.fuga.FugaRepository, 暫定和名2, get(FugaIdentifier), Fuga, [sut.piyo], [fuga], [], []]",
                        "[stub.domain.model.type.fuga.FugaRepository, 暫定和名2, register(Fuga), void, [], [], [], []]"
                );

        assertThat(sut.typeReportOn(TypePerspective.IDENTIFIER).rows())
                .extracting(reportRow -> reportRow.list().get(0))
                .containsSequence(
                        "stub.domain.model.type.SimpleIdentifier");

        assertThat(sut.typeReportOn(TypePerspective.ENUM).rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub.domain.model.kind."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.domain.model.kind.BehaviourEnum, , [A, B], [], [], , ◯, ]",
                        "[stub.domain.model.kind.ParameterizedEnum, , [A, B], [String param], [], ◯, , ]",
                        "[stub.domain.model.kind.PolymorphismEnum, , [A, B], [], [], , , ◯]",
                        "[stub.domain.model.kind.RichEnum, , [A, B], [String param], [], ◯, ◯, ◯]",
                        "[stub.domain.model.kind.SimpleEnum, , [A, B, C, D], [], [], , , ]"
                );
    }

    @TestConfiguration
    static class Config {
    }
}