package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testing.TestSupport;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JigTypesTest {

    @Test
    void クラス可視性の判定() {
        assertAll(
                () -> assertEquals(JigTypeVisibility.PUBLIC, TestSupport.buildJigType(PublicNested.class).visibility()),
                // 可視性はバイトコードにはpublicか否かしかなく、コンパイラがネストしたクラスのアクセス修飾子を
                // 自身のclassファイルのアクセスフラグではなく外側のInnerClasses属性へ記録するため、
                // protectedなネストクラス自体はPUBLICとして観測される。
                () -> assertEquals(JigTypeVisibility.PUBLIC, TestSupport.buildJigType(ProtectedNested.class).visibility()),
                () -> assertEquals(JigTypeVisibility.NOT_PUBLIC, TestSupport.buildJigType(DefaultNested.class).visibility()),
                () -> assertEquals(JigTypeVisibility.NOT_PUBLIC, TestSupport.buildJigType(PrivateNested.class).visibility())
        );
    }

    public static class PublicNested {
    }

    protected static class ProtectedNested {
    }

    static class DefaultNested {
    }

    private static class PrivateNested {
    }

    /**
     * package-info.java は通常 package-info.class を生成しないが、
     * RUNTIME保持のアノテーションを付けるとコンパイラが生成するようになる
     * （{@link org.dddjava.jig.application.ut.domain.model.RuntimeRetainedAnnotation}）。
     * このとき生成された package-info をドメインの型として扱わないことを確認する。
     */
    @Test
    void アノテーションつきのpackage_infoをドメインとして扱わない(@TempDir Path tempDir) {
        var configuration = Configuration.from(new JigSettings(
                tempDir, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE));
        var sourceBasePaths = TestSupport.sourceLocationsFor("org/dddjava/jig/application/ut/domain/model");
        var jigRepository = DefaultJigRepositoryFactory.init(configuration).createJigRepository(sourceBasePaths);
        var jigService = configuration.jigService();

        var typeIdentifier = TypeId.valueOf("org.dddjava.jig.application.ut.domain.model.package-info");

        var jigTypes = jigService.jigTypes(jigRepository);
        assertFalse(jigTypes.resolveJigType(typeIdentifier).isPresent(), "JigTypeに存在しない");

        var coreDomainJigTypes = jigService.coreDomainJigTypes(jigRepository);
        assertFalse(coreDomainJigTypes.jigTypes().contains(typeIdentifier), "domain coreには存在しない");
    }
}
