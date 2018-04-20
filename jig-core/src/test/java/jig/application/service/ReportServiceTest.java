package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.template.Reports;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class ReportServiceTest {

    @Autowired
    ReportService sut;

    @Autowired
    CharacteristicRepository repository;
    @Autowired
    RelationRepository relationRepository;
    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @Test
    void test() {
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
                            "[A,B]",
                            "String fugaText, Integer fugaInteger",
                            "[HogeUser]",
                            "",
                            "◯",
                            "◯"
                    );
        });
    }

    @TestConfiguration
    static class Config {
    }
}