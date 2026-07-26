package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.terms.TermOrigin;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testing.TestSupport;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Swagger のアノテーションに書かれた説明が用語になることの検証。
 *
 * 実ライブラリのアノテーションをそのまま読ませる。JIG は FQN で照合するため、
 * アノテーションの所在が変わると読み取れなくなる。
 */
class SwaggerTermTest {

    @TempDir
    static Path outputDirectory;

    static Glossary glossary;

    @BeforeAll
    static void 用語集を読み取る() {
        var configuration = Configuration.from(new JigSettings(
                outputDirectory, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE));
        var sourceBasePaths = TestSupport.sourceLocationsFor("org/dddjava/jig/infrastructure/javaproductreader/ut");

        glossary = DefaultJigRepositoryFactory.init(configuration)
                .createJigRepository(sourceBasePaths)
                .fetchGlossary();
    }

    @Test
    void Operationのsummaryがメソッドの用語になる() {
        Term term = termOf(TermKind.メソッド, "OrderApi");

        assertEquals("注文する", term.title());
        assertEquals(TermOrigin.Swagger, term.origin());
    }

    @Test
    void Schemaのdescriptionがクラスの用語になる() {
        Term term = termOf(TermKind.クラス, "OrderRequest");

        assertEquals("注文リクエスト", term.title());
        assertEquals(TermOrigin.Swagger, term.origin());
    }

    @Test
    void Javadocがあるものはそちらを採用しSwaggerの説明で上書きしない() {
        Term term = termOf(TermKind.クラス, "CustomerRequest");

        assertEquals("Javadocの顧客リクエスト", term.title());
        assertEquals(TermOrigin.Javadoc, term.origin());
    }

    private static Term termOf(TermKind termKind, String simpleName) {
        return glossary.terms().stream()
                .filter(term -> term.termKind() == termKind)
                .filter(term -> term.id().asText().contains(simpleName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        simpleName + " の" + termKind + "の用語がありません: " + glossary.terms()));
    }
}
