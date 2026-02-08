package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.infrastructure.javaparser.ut.ParseTargetCanonicalClass;
import org.dddjava.jig.infrastructure.javaparser.ut.ParseTargetNestedClass;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import testing.TestSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaparserReaderTest {

    JavaparserReader sut;

    @BeforeEach
    void setUp() {
        // javaparserReaderの中でCompilationUnitの初期設定を行っているため、parseより前に呼び出す必要がある。
        sut = new JavaparserReader();
    }

    @CsvSource({
            "ut/package_info_javadoc,          package-info.java, this is term title",
            "ut/package_info_block_comment,    package-info.java, package_info_block_comment", // javadocでないものはそのままパッケージ名がtitleになる
            "ut/package_info_no_comment,       package-info.java, package_info_no_comment", // コメントがないものはそのままパッケージ名がtitleになる
            "ut/package_info_javadoc_tag_only, package-info.java, package_info_javadoc_tag_only", // javadocタグのみのものはパッケージ名がtitleになる
    })
    @ParameterizedTest
    void PackageInfoからタイトルを読み取れる(String packagePathText, String filePathText, String expected) {
        Path targetPath = getJavaFilePath(Path.of(packagePathText, filePathText));
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.loadPackageInfoJavaFile(targetPath, glossaryRepository);

        PackageId packageId = TestSupport.getTypeIdFromClass(this.getClass())
                .packageId().subpackageOf(packagePathText.split("/"));
        Term term = glossaryRepository.get(packageId);

        assertEquals(expected, term.title());
    }

    @Test
    void 典型的なPackageInfoを読み取れる() {
        Path path = Path.of("ut/package_info_typical", "package-info.java");
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.loadPackageInfoJavaFile(getJavaFilePath(path), glossaryRepository);

        PackageId packageId = TestSupport.getTypeIdFromClass(this.getClass())
                .packageId().subpackageOf("ut", "package_info_typical");
        Term term = glossaryRepository.get(packageId);

        assertEquals("色々書いているpackage-info", term.title());
        assertEquals("""
                １行目がタイトルとして採用。２行目以降に書かれているものが本文として読み取られる。
                ここに記述されている linkタグ や codeタグ はテキストとして可読な形に置き換えられる。
                インラインでないJavadocタグは本文には含まれない。""", term.description());
    }

    @Test
    void 典型的なクラスから読み取れる() {
        Path path = Path.of("ut", "ParseTargetCanonicalClass.java");
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.parseJavaFile(getJavaFilePath(path), glossaryRepository);

        var glossary = glossaryRepository.all();
        var term = glossary.termOf(TestSupport.getTypeIdFromClass(ParseTargetCanonicalClass.class).value(), TermKind.クラス);

        assertEquals("クラスコメント", term.title());
    }

    @Test
    void 典型的なクラスからメソッドを読み取れる() {
        Path path = Path.of("ut", "ParseTargetCanonicalClass.java");
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.parseJavaFile(getJavaFilePath(path), glossaryRepository);

        var glossary = glossaryRepository.all();
        var term = glossary.termOf(JigMethodId.from(
                TestSupport.getTypeIdFromClass(ParseTargetCanonicalClass.class),
                "method", List.of()).value(), TermKind.メソッド);

        assertEquals("メソッドコメント", term.title());
    }

    @Test
    void 典型的なクラスからフィールドを読み取れる() {
        Path path = Path.of("ut", "ParseTargetCanonicalClass.java");
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.parseJavaFile(getJavaFilePath(path), glossaryRepository);

        var glossary = glossaryRepository.all();
        var term = glossary.termOf(JigFieldId.from(
                TestSupport.getTypeIdFromClass(ParseTargetCanonicalClass.class),
                "field").value(), TermKind.フィールド);

        assertEquals("フィールドコメント", term.title());
    }

    @Test
    void ネストしたクラスとメソッドを読み取れる() {
        Path path = Path.of("ut", "ParseTargetNestedClass.java");
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.parseJavaFile(getJavaFilePath(path), glossaryRepository);

        var glossary = glossaryRepository.all();
        var outerTerm = glossary.termOf(
                TestSupport.getTypeIdFromClass(ParseTargetNestedClass.class).value(),
                TermKind.クラス
        );
        var innerTypeId = TypeId.valueOf("org.dddjava.jig.infrastructure.javaparser.ut.ParseTargetNestedClass.Inner");
        var innerTerm = glossary.termOf(innerTypeId.value(), TermKind.クラス);
        var innerMethodTerm = glossary.termOf(
                JigMethodId.from(innerTypeId, "innerMethod", List.of()).value(),
                TermKind.メソッド
        );

        assertEquals("外側クラスコメント", outerTerm.title());
        assertEquals("内側クラスコメント", innerTerm.title());
        assertEquals("内側メソッドコメント", innerMethodTerm.title());
    }

    private Path getJavaFilePath(Path requireJavaFilePath) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        Class<?> callerClass = walker.walk(stackFrames -> stackFrames.skip(1).findFirst().orElseThrow())
                // DeclaringClassをとるために RETAIN_CLASS_REFERENCE を指定している。
                // 1クラスだけなので getClassName から Class.forName の方がいい気はしないでもない。
                .getDeclaringClass();

        try (var pathStream = Files.find(TestSupport.getTestSourceRootPath(),
                10, (path, basicFileAttributes) -> path.endsWith(callerClass.getSimpleName() + ".java"))) {
            Path callerClassJavaFilePath = pathStream.findAny().orElseThrow();

            return callerClassJavaFilePath.resolveSibling(requireJavaFilePath);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
