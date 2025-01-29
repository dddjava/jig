package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class JavaparserReaderTest {

    JavaparserReader sut;

    @BeforeEach
    void setUp() {
        // javaparserReaderの中でCompilationUnitの初期設定を行っているため、parseより前に呼び出す必要がある。
        sut = new JavaparserReader(null);
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
        CompilationUnit cu = StaticJavaParser.parse(code);
        Optional<PackageComment> packageComment = sut.readPackageComment(cu);
        assertTrue(packageComment.isEmpty());
    }

    @MethodSource
    @ParameterizedTest
    void コメントが取得できる(String code, String expectedTitle, String expectedBody) {
        CompilationUnit cu = StaticJavaParser.parse(code);

        Optional<PackageComment> packageComment = sut.readPackageComment(cu);

        PackageComment actual = packageComment.orElseThrow();
        assertEquals(expectedTitle, actual.asText());
        assertEquals(expectedBody, actual.descriptionComment().bodyText()
                // FIXME javaparserを通すとline.separatorの改行コードになるのの暫定対応
                //  StaticJavaParser#parse(String) 使用時のみ？ #parse(Path) のファイルが\nの場合はこういうのは\nのままの模様
                .replace("\r\n", "\n"));
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