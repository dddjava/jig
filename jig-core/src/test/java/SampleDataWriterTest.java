import org.dddjava.jig.adapter.html.*;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;
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
        var persistenceAccessorsRepository = repository.jigDataProvider().persistenceAccessorRepository();
        var outputAdapters = OutputAdapters.from(jigTypes, persistenceAccessorsRepository);

        // outputs-data.js
        {
            var json = OutputsSummaryAdapter.buildJson(outputAdapters);
            Path sampleFile = Path.of("src/main/resources/templates/data/outputs-data.js");
            Files.writeString(sampleFile,
                "// 表示確認用のサンプルデータ\n" +
                "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
                "globalThis.outputPortData = " + json + "\n");
        }

        // glossary-data.js
        {
            var glossary = jigService.glossary(repository);
            var json = GlossaryAdapter.buildJson(glossary);
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
            var typeRelationships = jigService.typeRelationships(repository);
            var json = PackageSummaryAdapter.buildJson(jigPackages, packageRelations, typeRelationships);
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
    }
}
