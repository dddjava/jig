package org.dddjava.jig.presentation.controller.classlist;

import org.dddjava.jig.application.usecase.ImportService;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.infrastructure.LocalProject;
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
    ClassListController sut;

    @Autowired
    ImportService importService;
    @Autowired
    LocalProject localProject;

    @Test
    void stubパッケージを対象に各レポートの出力を検証する() throws Exception {
        importService.importSources(localProject.getSpecificationSources(), localProject.getSqlSources(), localProject.getTypeNameSources(), localProject.getPackageNameSources());

        assertThat(sut.serviceReport().rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.application.service.CanonicalService, サービス和名, fuga(FugaIdentifier), Fuga, , [HogeRepository, FugaRepository], [HogeRepository.method(), FugaRepository.get(FugaIdentifier)]]",
                        "[stub.application.service.CanonicalService, サービス和名, method(), void, , [], []]",
                        "[stub.application.service.SimpleService, フィールドを持たないサービス, コントローラーから呼ばれない(), void, , [], []]",
                        "[stub.application.service.SimpleService, フィールドを持たないサービス, コントローラーから呼ばれる(), void, ◯, [], []]"
                );

        assertThat(sut.datasourceReport().rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsSequence(
                        "[stub.domain.model.type.fuga.FugaRepository, リポジトリ和名, get(FugaIdentifier), Fuga, [sut.piyo], [fuga], [], []]",
                        "[stub.domain.model.type.fuga.FugaRepository, リポジトリ和名, register(Fuga), void, [], [], [], []]"
                );

        assertThat(sut.typeReportOn(Characteristic.IDENTIFIER).rows())
                .extracting(reportRow -> reportRow.list().get(0))
                .containsSequence(
                        "stub.domain.model.type.SimpleIdentifier");

        assertThat(sut.enumReport().rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub.domain.model.kind."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.domain.model.kind.BehaviourEnum, , [A, B], [], [], , ◯, ]",
                        "[stub.domain.model.kind.ParameterizedEnum, , [A, B], [String param], [], ◯, , ]",
                        "[stub.domain.model.kind.PolymorphismEnum, , [A, B], [], [], , , ◯]",
                        "[stub.domain.model.kind.RichEnum, , [A, B], [String param], [], ◯, ◯, ◯]",
                        "[stub.domain.model.kind.SimpleEnum, , [A, B, C, D], [], [], , , ]"
                );

        assertThat(sut.decisionReport().rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub.domain.model.relation."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.domain.model.relation.DecisionClass, ifがあるメソッド()]",
                        "[stub.domain.model.relation.DecisionClass, switchがあるメソッド()]"
                );
    }

    @TestConfiguration
    static class Config {

        @Bean
        LocalProject localProject() {
            // jig-coreプロジェクトを読み取り対象にする
            return new LocalProject(
                    TestSupport.getModuleRootPath().toString(),
                    // classの出力ディレクトリ
                    Paths.get(TestSupport.defaultPackageClassURI()).toString(),
                    "src/test/resources",
                    // 日本語取得のためのソース読み取り場所
                    "src/test/java");
        }
    }
}