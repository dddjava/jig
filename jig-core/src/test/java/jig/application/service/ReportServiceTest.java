package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.report.Reports;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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

        Identifier identifier = new Identifier("test.HogeEnum");
        repository.register(identifier, Characteristic.ENUM);
        repository.register(identifier, Characteristic.ENUM_BEHAVIOUR);
        repository.register(identifier, Characteristic.ENUM_PARAMETERIZED);
        repository.register(identifier, Characteristic.ENUM_POLYMORPHISM);

        japaneseNameRepository.register(identifier, new JapaneseName("対応する和名"));
        relationRepository.register(new Relation(new Identifier("test.HogeUser"), identifier, RelationType.FIELD));

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
                            "[t.HogeUser]",
                            "false",
                            "false",
                            "false"
                    );
        });
    }

    @ComponentScan("jig")
    @Configuration
    static class Config {
    }
}