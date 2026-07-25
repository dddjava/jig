package org.dddjava.jig.contract;

import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.fixtures.FixtureProject;
import org.dddjava.jig.fixtures.JigFixtures;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 代表プロジェクト showcase からサイトを生成する。
 *
 * 生成の手順をここへ集め、契約ごとのテストは観測に集中する。
 */
final class ShowcaseSite {

    static final String FIXTURE = "showcase";
    static final int FIXTURE_RELEASE = 21;

    private ShowcaseSite() {
    }

    /**
     * クラスファイルとソースを入力に生成する。
     *
     * @return 生成されたドキュメント
     */
    static List<JigDocument> generateTo(Path outputDirectory) {
        return generate(outputDirectory, new SourceBasePath(List.of(fixture().sources())));
    }

    /**
     * クラスファイルだけを入力に生成する。
     *
     * @return 生成されたドキュメント
     */
    static List<JigDocument> generateBytecodeOnlyTo(Path outputDirectory) {
        return generate(outputDirectory, new SourceBasePath(List.of()));
    }

    private static List<JigDocument> generate(Path outputDirectory, SourceBasePath javaFileBasePath) {
        JigSettings settings = new JigSettings(
                outputDirectory,
                Optional.of("showcase.domain.+"),
                JigDocument.canonical(),
                Locale.JAPANESE);

        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new SourceBasePath(List.of(fixture().classes(FIXTURE_RELEASE))),
                javaFileBasePath);

        return JigExecutor.standard(Configuration.from(settings), sourceBasePaths).listResult().stream()
                .map(result -> result.jigDocument())
                .toList();
    }

    private static FixtureProject fixture() {
        return JigFixtures.project(FIXTURE);
    }
}
