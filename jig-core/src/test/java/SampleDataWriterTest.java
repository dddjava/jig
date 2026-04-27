import org.dddjava.jig.adapter.datajs.*;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.Test;
import testing.JigTest;
import testing.TestSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@JigTest
class SampleDataWriterTest {

    @Test
    void サンプルデータを書き出す(Configuration configuration, JigService jigService) throws IOException {
        // sampleパッケージのみスキャン
        var sampleBasePaths = new SourceBasePaths(
                new SourceBasePath(Collections.singletonList(
                        Paths.get(TestSupport.defaultPackageClassURI()).resolve("sample"))),
                new SourceBasePath(Collections.singletonList(
                        TestSupport.getTestSourceRootPath().resolve("sample")))
        );

        var factory = DefaultJigRepositoryFactory.init(configuration);
        var repository = factory.createJigRepository(sampleBasePaths);

        var jigTypes = jigService.jigTypes(repository);
        var externalAccessorRepositories = repository.externalAccessorRepositories();
        var outboundAdapters = OutboundAdapters.from(jigTypes, externalAccessorRepositories);

        writeDataJs("outbound-data.js", "outboundData",
                OutboundDataAdapter.buildOutboundJson(outboundAdapters, externalAccessorRepositories));

        var domainPackageRoots = jigService.coreDomainJigTypes(repository).domainPackageRoots();
        writeDataJs("glossary-data.js", "glossaryData",
                GlossaryDataAdapter.buildGlossaryJson(jigService.glossary(repository), domainPackageRoots));

        writeDataJs("package-data.js", "packageData",
                PackageDataAdapter.buildPackageJson(jigService.packages(repository), jigService.packageRelations(repository), domainPackageRoots));

        writeDataJs("insight-data.js", "insightData",
                InsightDataAdapter.buildInsightJson(jigService.insights(repository)));

        writeDataJs("list-output-data.js", "listData",
                ListOutputDataAdapter.buildListJson(repository, jigService));

        writeDataJs("inbound-data.js", "inboundData",
                InboundDataAdapter.buildInboundJson(jigService.inboundAdapters(repository), jigTypes));

        writeDataJs("usecase-data.js", "usecaseData",
                UsecaseDataAdapter.buildUsecaseJson(jigService.serviceTypes(repository)));

        var coreDomainJigTypes = jigService.coreDomainJigTypes(repository);
        if (!coreDomainJigTypes.isEmpty()) {
            var enumModels = repository.jigDataProvider().fetchEnumModels();
            writeDataJs("domain-data.js", "domainData",
                    DomainDataAdapter.buildDomainJson(coreDomainJigTypes, coreDomainJigTypes.jigTypes(), enumModels));

            var typeRelationsJson = Json.object("relations", Json.arrayObjects(jigService.typeRelationships(repository).list().stream()
                    .map(relation -> Json.object("from", relation.from().fqn())
                            .and("to", relation.to().fqn()))
                    .toList())).build();
            writeDataJs("type-relations-data.js", "typeRelationsData", typeRelationsJson);
        }
    }

    private void writeDataJs(String fileName, String globalVarName, String json) throws IOException {
        Path file = Path.of("src/main/resources/templates/data/" + fileName);
        Files.writeString(file,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis." + globalVarName + " = " + json + "\n");
    }
}
