package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import testing.TestSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JavaparserReaderTest {

    JavaparserReader sut;

    @BeforeEach
    void setUp() {
        // javaparserReaderの中でCompilationUnitの初期設定を行っているため、parseより前に呼び出す必要がある。
        sut = new JavaparserReader();
    }

    @CsvSource({
            "ut/my_package_1, package-info.java, this is term title",
            "ut/my_package_2, package-info.java, my_package_2", // javadocでないものはそのままパッケージ名がtitleになる
            "ut/my_package_3, package-info.java, my_package_3", // コメントがないものはそのままパッケージ名がtitleになる
    })
    @ParameterizedTest
    void パッケージコメントの読み取り(String packagePathText, String filePathText, String expected) throws IOException {
        Path targetPath = getJavaFilePath(Path.of(packagePathText, filePathText));
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        sut.loadPackageInfoJavaFile(targetPath, glossaryRepository);

        PackageIdentifier packageIdentifier = TypeIdentifier.from(this.getClass())
                .packageIdentifier().subpackageOf(packagePathText.split("/"));
        Term term = glossaryRepository.get(packageIdentifier);

        assertEquals(expected, term.title());
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

    @ValueSource(strings = {"""
            package org.dddjava.jig.my_package;
            """, """
            // ラインコメント
            package org.dddjava.jig.my_package;
            """, """
            /*
             * ブロックコメント
             */
            package org.dddjava.jig.my_package;
            """, """
            package org.dddjava.jig.my_package;
            /**
             * 後ろにあるJavadoc風コメント
             */
            """, """
            /**
             * @see javadocタグのみある
             */
            package org.dddjava.jig.my_package;
            """
    })
    @ParameterizedTest
    void JavadocコメントのないものはエラーにならずPackageCommentも生成されない(String code) {
        GlossaryRepository mock = mock(GlossaryRepository.class);
        CompilationUnit cu = StaticJavaParser.parse(code);

        sut.loadPackageInfoJavaFile(cu, mock);

        verify(mock, never()).register(any());
    }

    @MethodSource
    @ParameterizedTest
    void コメントが取得できる(String code, String expectedTitle, String expectedBody) {
        GlossaryRepository mock = mock(GlossaryRepository.class);
        CompilationUnit cu = StaticJavaParser.parse(code);

        sut.loadPackageInfoJavaFile(cu, mock);

        verify(mock).register(Mockito.argThat(term ->
                term.title().equals(expectedTitle) && term.description().equals(expectedBody)));
    }

    static Stream<Arguments> コメントが取得できる() {
        return Stream.of(
                argumentSet("概要のみ",
                        """
                                /**
                                 * packageにつけられたコメント
                                 */
                                package org.dddjava.jig.my_package;
                                """,
                        "packageにつけられたコメント",
                        ""),
                argumentSet("概要と本文",
                        """
                                /**
                                 * 概要です。
                                 *
                                 * 本文です。
                                 */
                                package org.dddjava.jig.my_package;
                                """,
                        "概要です",
                        "本文です。"
                ),
                argumentSet("概要と本文",
                        """
                                /**
                                 * packageにつけられたコメント。ここからが本文です。複文もOK
                                 *
                                 * 改行されたものも入ります。
                                 * 末尾の改行は入りません。
                                 *
                                 * @link javadocタグは入りません。
                                 * @original 独自タグも入りません。
                                 */
                                package org.dddjava.jig.my_package;
                                """,
                        "packageにつけられたコメント",
                        """
                                ここからが本文です。複文もOK
                                
                                改行されたものも入ります。
                                末尾の改行は入りません。"""
                ),
                argumentSet("インラインタグ（@link）をただの文字列にする",
                        """
                                /**
                                 * 概要に使われたインラインタグ {@link hoge.fuga text} をただの{@link テキスト}にします。
                                 *
                                 * 本文に使われたインラインタグ {@link hoge.fuga text} をただの{@link テキスト }にします
                                 * 本文に使われたインラインタグ {@link hoge.fuga text} をただの{@link テキスト }にします
                                 */
                                package org.dddjava.jig.my_package;
                                """,
                        "概要に使われたインラインタグ text をただのテキストにします",
                        """
                                本文に使われたインラインタグ text をただのテキストにします
                                本文に使われたインラインタグ text をただのテキストにします"""
                )
        );
    }
}