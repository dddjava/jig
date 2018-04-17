package jig.gradle;

import jig.application.service.AnalyzeService;
import jig.application.service.DependencyService;
import jig.application.service.ReportService;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.PrefixRemoveIdentifierFormatter;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.JavaparserJapaneseReader;
import jig.infrastructure.mybatis.MyBatisSqlReader;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;

public class ServiceFactory {

    final CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    final RelationRepository relationRepository = new OnMemoryRelationRepository();
    final SqlRepository sqlRepository = new OnMemorySqlRepository();
    final JigPaths jigPaths = new JigPaths(
            "build/classes/main",
            "build/resources/main",
            "src/main/java"
    );

    AnalyzeService analyzeService() {
        return new AnalyzeService(
                new AsmClassFileReader(),
                new MyBatisSqlReader(),
                new JavaparserJapaneseReader(
                        new OnMemoryJapaneseNameRepository(),
                        jigPaths
                ),
                new DependencyService(
                        characteristicRepository,
                        relationRepository
                ),
                jigPaths,
                sqlRepository
        );
    }

    ReportService reportService(String outputOmitPrefixPath) {
        return new ReportService(
                characteristicRepository,
                relationRepository,
                sqlRepository,
                new OnMemoryJapaneseNameRepository(),
                new PrefixRemoveIdentifierFormatter(outputOmitPrefixPath)
        );
    }


}
