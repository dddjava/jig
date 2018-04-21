package jig.application.usecase;

import jig.application.service.DependencyService;
import jig.application.service.SpecificationService;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.datasource.SqlReader;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.project.SourceFactory;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.template.Reports;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class ReportServiceTest {

    @Autowired
    ReportService sut;

    @Autowired
    DependencyService dependencyService;
    @Autowired
    SpecificationService specificationService;
    @Autowired
    SqlReader sqlReader;
    @Autowired
    SqlRepository sqlRepository;

    @Autowired
    CharacteristicRepository repository;
    @Autowired
    RelationRepository relationRepository;
    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @Test
    void 出力だけのテスト() {
        TypeIdentifier typeIdentifier = new TypeIdentifier("test.HogeEnum");

        TypeCharacteristics typeCharacteristics = new TypeCharacteristics(
                typeIdentifier,
                Stream.of(
                        Characteristic.ENUM,
                        //Characteristic.ENUM_PARAMETERIZED,
                        Characteristic.ENUM_BEHAVIOUR,
                        Characteristic.ENUM_POLYMORPHISM
                ).collect(Collectors.toSet()));
        repository.register(typeCharacteristics);

        relationRepository.registerField(new FieldDeclaration(typeIdentifier, "fugaText", new TypeIdentifier(("java.lang.String"))));
        relationRepository.registerField(new FieldDeclaration(typeIdentifier, "fugaInteger", new TypeIdentifier(("java.lang.Integer"))));

        japaneseNameRepository.register(typeIdentifier, new JapaneseName("対応する和名"));
        relationRepository.registerField(new FieldDeclaration(new TypeIdentifier("test.HogeUser"), "hogera", typeIdentifier));
        relationRepository.registerConstants(new FieldDeclaration(typeIdentifier, "A", typeIdentifier));
        relationRepository.registerConstants(new FieldDeclaration(typeIdentifier, "B", typeIdentifier));

        Reports reports = sut.reports();
        reports.each(report -> {
            if (!report.title().value().equals("ENUM")) {
                return;
            }

            assertThat(report.rows().size()).isEqualTo(1);
            assertThat(report.rows().get(0).list())
                    .containsExactly(
                            "test.HogeEnum",
                            "対応する和名",
                            "[A, B]",
                            "[String fugaText, Integer fugaInteger]",
                            "[HogeUser]",
                            "",
                            "◯",
                            "◯"
                    );
        });
    }

    @Test
    void クラスを読み込むE2Eに近いテスト() throws Exception {
        // 読み込む対象のソースを取得
        Path path = Paths.get(TestSupport.defaultPackageClassURI());
        JigPaths jigPaths = new JigPaths(path.toString(),
                // Mapper.xmlのためだが、ここではHitしなくてもテストのクラスパスから読めてしまう
                "not/read/resources",
                // TODO ソースディレクトリの安定した取得方法が欲しい
                "not/read/sources");
        SourceFactory sourceFactory = new SourceFactory(jigPaths, path);

        dependencyService.registerSpecifications(
                specificationService.specification(
                        jigPaths.getSpecificationSources(sourceFactory)));
        sqlRepository.register(
                sqlReader.readFrom(
                        jigPaths.getSqlSources(sourceFactory)));

        japaneseNameRepository.register(new TypeIdentifier(CanonicalService.class), new JapaneseName("暫定和名1"));
        assertThat(sut.methodReportOn(MethodPerspective.SERVICE).rows())
                .extracting(reportRow -> reportRow.list().toString())
                .containsSequence(
                        "[stub.application.service.CanonicalService, 暫定和名1, fuga(FugaIdentifier), Fuga, [HogeRepository, FugaRepository], [HogeRepository.method(), FugaRepository.get(FugaIdentifier)]]",
                        "[stub.application.service.CanonicalService, 暫定和名1, method(), void, [], []]"
                );

        japaneseNameRepository.register(new TypeIdentifier(FugaRepository.class), new JapaneseName("暫定和名2"));
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
                .extracting(reportRow -> reportRow.list().get(0))
                .containsSequence(
                        "stub.domain.model.kind.BehaviourEnum",
                        "stub.domain.model.kind.ParameterizedEnum",
                        "stub.domain.model.kind.PolymorphismEnum",
                        "stub.domain.model.kind.RichEnum",
                        "stub.domain.model.kind.SimpleEnum");
    }

    @TestConfiguration
    static class Config {
    }
}