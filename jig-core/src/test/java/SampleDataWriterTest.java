import org.dddjava.jig.adapter.html.OutputsSummaryAdapter;
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
        var persistenceAccessorsRepository = repository.jigDataProvider().persistenceAccessorsRepository();
        var outputAdapters = OutputAdapters.from(jigTypes, persistenceAccessorsRepository);

        var json = OutputsSummaryAdapter.buildJson(outputAdapters);

        Path sampleFile = Path.of("src/main/resources/templates/data/outputs-data.js");
        Files.writeString(sampleFile,
            "// 表示確認用のサンプルデータ\n" +
            "// このファイルは" + this.getClass().getSimpleName() + "によって自動生成されます。手動で変更しないでください。\n" +
            "globalThis.outputPortData = " + json + "\n");
    }
}
