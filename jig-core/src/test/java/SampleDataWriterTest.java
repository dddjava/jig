import org.dddjava.jig.adapter.html.*;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.inbound.InputAdapters;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;
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

        // outbound-data.js
        {
            var json = OutboundSummaryAdapter.buildJson(outboundAdapters);
            Path sampleFile = Path.of("src/main/resources/templates/data/outbound-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.outboundData = " + json + "\n");
        }

        // glossary-data.js
        {
            var glossary = jigService.glossary(repository);
            var domainPackageRoots = jigService.coreDomainJigTypes(repository).packageFilterCandidates();
            var json = GlossaryAdapter.buildJson(glossary, domainPackageRoots);
            Path sampleFile = Path.of("src/main/resources/templates/data/glossary-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.glossaryData = " + json + "\n");
        }

        // package-data.js
        {
            var jigPackages = jigService.packages(repository);
            var packageRelations = jigService.packageRelations(repository);
            var domainPackageRoots = jigService.coreDomainJigTypes(repository).packageFilterCandidates();
            var json = PackageSummaryAdapter.buildJson(jigPackages, packageRelations, domainPackageRoots);
            Path sampleFile = Path.of("src/main/resources/templates/data/package-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.packageData = " + json + "\n");
        }

        // insight-data.js
        {
            var insights = jigService.insights(repository);
            var json = InsightAdapter.buildJson(insights);
            Path sampleFile = Path.of("src/main/resources/templates/data/insight-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.insightData = " + json + "\n");
        }

        // list-output-data.js
        {
            var json = ListOutputAdapter.buildJson(repository, jigService, configuration.jigDocumentContext());
            Path sampleFile = Path.of("src/main/resources/templates/data/list-output-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.listData = " + json + "\n");
        }

        // inbound-data.js
        {
            var inputAdapters = jigService.inputAdapters(repository);
            var json = InboundSummaryAdapter.buildJson(inputAdapters, jigTypes);
            Path sampleFile = Path.of("src/main/resources/templates/data/inbound-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.inboundData = " + json + "\n");
        }

        // usecase-data.js
        {
            var serviceTypes = jigService.serviceTypes(repository);
            var json = UsecaseSummaryAdapter.buildJson(serviceTypes);
            Path sampleFile = Path.of("src/main/resources/templates/data/usecase-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.usecaseData = " + json + "\n");
        }

        // domain-data.js and type-relations-data.js
        {
            var coreDomainJigTypes = jigService.coreDomainJigTypes(repository);
            if (!coreDomainJigTypes.empty()) {
                var domainJigTypes = coreDomainJigTypes.jigTypes();
                var packageList = JigPackageWithJigTypes.listWithParent(domainJigTypes);
                var enumModels = repository.jigDataProvider().fetchEnumModels();

                var json = DomainSummaryAdapter.buildJson(packageList, domainJigTypes, enumModels);
                Path sampleFile = Path.of("src/main/resources/templates/data/domain-data.js");
                Files.writeString(sampleFile,
                    "// 表示確認用のサンプルデータ\n" +
                    "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                    "globalThis.domainData = " + json + "\n");

                var typeRelationships = jigService.typeRelationships(repository);
                var typeRelationsJson = Json.object("relations", Json.arrayObjects(typeRelationships.list().stream()
                        .map(relation -> Json.object("from", relation.from().fqn())
                                .and("to", relation.to().fqn()))
                        .toList())).build();
                Path typeRelationsFile = Path.of("src/main/resources/templates/data/type-relations-data.js");
                Files.writeString(typeRelationsFile,
                    "// 表示確認用のサンプルデータ\n" +
                    "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                    "globalThis.typeRelationsData = " + typeRelationsJson + "\n");
            }
        }
    }
}
