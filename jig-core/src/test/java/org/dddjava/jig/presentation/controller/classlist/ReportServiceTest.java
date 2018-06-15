package org.dddjava.jig.presentation.controller.classlist;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.DefaultLayout;
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
    ImplementationService implementationService;
    @Autowired
    LocalProject localProject;

    @Test
    void stubパッケージを対象に各レポートの出力を検証する() {
        ProjectData projectData = implementationService.readProjectData(localProject);

        assertThat(sut.serviceReport(projectData).rows())
                .filteredOn(reportRow -> reportRow.list().get(0).startsWith("stub."))
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly(
                        "[stub.application.service.CanonicalService, fuga(FugaIdentifier), Fuga, , サービス和名, , 普通のドメインモデル, [普通の識別子], [FugaRepository, HogeRepository], [FugaRepository.get(FugaIdentifier), HogeRepository.method()]]",
                        "[stub.application.service.CanonicalService, method(), void, , サービス和名, , , [], [], []]",
                        "[stub.application.service.DecisionService, 分岐のあるメソッド(Object), void, , 分岐のあるサービス, , , [], [], []]",
                        "[stub.application.service.SimpleService, RESTコントローラーから呼ばれる(), void, ◯, フィールドを持たないサービス, , , [], [], []]",
                        "[stub.application.service.SimpleService, コントローラーから呼ばれない(), void, , フィールドを持たないサービス, , , [], [], []]",
                        "[stub.application.service.SimpleService, コントローラーから呼ばれる(), void, ◯, フィールドを持たないサービス, , , [], [], []]"
                );

        assertThat(sut.datasourceReport(projectData).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsSequence(
                        "[stub.domain.model.type.fuga.FugaRepository, get(FugaIdentifier), Fuga, リポジトリ和名, [sut.piyo], [fuga], [], []]",
                        "[stub.domain.model.type.fuga.FugaRepository, register(Fuga), void, リポジトリ和名, [], [], [], []]"
                );

        assertThat(sut.decisionReport(projectData, Layer.APPLICATION).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly("[stub.application.service.DecisionService, 分岐のあるメソッド(Object)]");
        assertThat(sut.decisionReport(projectData, Layer.DATASOURCE).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly("[stub.infrastructure.datasource.DecisionDatasource, 分岐のあるメソッド(Object)]");
        assertThat(sut.decisionReport(projectData, Layer.PRESENTATION).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsExactly("[stub.presentation.controller.DecisionController, 分岐のあるメソッド(Object)]");
    }

    @TestConfiguration
    static class Config {

        @Bean
        LocalProject localProject() {
            // jig-coreプロジェクトを読み取り対象にする
            return new LocalProject(new DefaultLayout(
                    TestSupport.getModuleRootPath().toString(),
                    // classの出力ディレクトリ
                    Paths.get(TestSupport.defaultPackageClassURI()).toString(),
                    "src/test/resources",
                    // 日本語取得のためのソース読み取り場所
                    "src/test/java"));
        }
    }
}
