package org.dddjava.jig.contract;

import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.fixtures.FixtureProject;
import org.dddjava.jig.fixtures.JigFixtures;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * サポートするJavaバージョンでコンパイルされたクラスファイルを、同じように解析できることの契約。
 *
 * 検証点は docs/adr/java_version_support_policy.md の「下限・toolchainと同じ・最新のLTS」に対応する。
 */
class BytecodeCompatibilityContractTest {

    static final int LOWER_BOUND_RELEASE = 8;
    static final int TOOLCHAIN_RELEASE = 21;
    static final int LATEST_LTS_RELEASE = 25;

    @ParameterizedTest
    @ValueSource(ints = {LOWER_BOUND_RELEASE, LATEST_LTS_RELEASE})
    void クラスファイルのバージョンが変わっても同じ型情報が得られる(int release) {
        assertEquals(analyze(TOOLCHAIN_RELEASE), analyze(release));
    }

    @Test
    void 下限バージョンのクラスファイルから型とメンバーを読み取れる() {
        String actual = analyze(LOWER_BOUND_RELEASE);

        assertTrue(actual.contains("fixture.compat.CompatValue CLASS"), actual);
        assertTrue(actual.contains("fixture.compat.CompatBehavior INTERFACE"), actual);
        assertTrue(actual.contains("fixture.compat.CompatKind ENUM"), actual);
        assertTrue(actual.contains("apply"), actual);
    }

    /**
     * 型・種別・メンバー名を、宣言順や読み取り順に依存しない文字列へまとめる。
     */
    private static String analyze(int release) {
        FixtureProject project = JigFixtures.project("bytecode-compat");
        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new SourceBasePath(List.of(project.classes(release))),
                new SourceBasePath(List.of(project.sources())));

        JigRepository repository = DefaultJigRepositoryFactory.init(configuration())
                .createJigRepository(sourceBasePaths);

        return summarize(repository.fetchJigTypes());
    }

    private static String summarize(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .map(jigType -> jigType.fqn()
                        + " " + jigType.jigTypeHeader().javaTypeDeclarationKind()
                        + jigType.allJigMethodStream()
                        .map(JigMethod::name)
                        .sorted()
                        .collect(Collectors.joining(",", "(", ")")))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private static Configuration configuration() {
        try {
            JigSettings settings = new JigSettings(
                    Files.createTempDirectory("jig-contract"),
                    Optional.of("fixture.+"),
                    JigDocument.canonical(),
                    Locale.JAPANESE);
            return Configuration.from(settings);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
