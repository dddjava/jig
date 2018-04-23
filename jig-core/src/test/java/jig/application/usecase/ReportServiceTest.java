package jig.application.usecase;

import jig.domain.model.project.SourceFactory;
import jig.domain.model.report.MethodPerspective;
import jig.domain.model.report.TypePerspective;
import jig.infrastructure.JigPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import testing.TestConfiguration;
import testing.TestSupport;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class ReportServiceTest {

    @Autowired
    ReportService sut;

    @Autowired
    ImportLocalProjectService importLocalProjectService;

    @Test
    void stubパッケージを対象に各レポートの出力を検証する() throws Exception {
        importLocalProjectService.importProject();

        assertThat(sut.methodReportOn(MethodPerspective.SERVICE).rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.application.service.CanonicalService, サービス和名, fuga(FugaIdentifier), Fuga, , [HogeRepository, FugaRepository], [HogeRepository.method(), FugaRepository.get(FugaIdentifier)]]",
                        "[stub.application.service.CanonicalService, サービス和名, method(), void, , [], []]",
                        "[stub.application.service.SimpleService, フィールドを持たないサービス, コントローラーから呼ばれない(), void, , [], []]",
                        "[stub.application.service.SimpleService, フィールドを持たないサービス, コントローラーから呼ばれる(), void, ◯, [], []]"
                );

        assertThat(sut.methodReportOn(MethodPerspective.REPOSITORY).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsSequence(
                        "[stub.domain.model.type.fuga.FugaRepository, リポジトリ和名, get(FugaIdentifier), Fuga, [sut.piyo], [fuga], [], []]",
                        "[stub.domain.model.type.fuga.FugaRepository, リポジトリ和名, register(Fuga), void, [], [], [], []]"
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

        @Bean
        SourceFactory sourceFactory() {
            // jig-coreプロジェクトを読み取り対象にする
            JigPaths jigPaths = new JigPaths(
                    TestSupport.getModuleRootPath().toString(),
                    // classの出力ディレクトリ
                    Paths.get(TestSupport.defaultPackageClassURI()).toString(),
                    "src/test/resources",
                    // 日本語取得のためのソース読み取り場所
                    "src/test/java");
            return jigPaths;
        }
    }
}