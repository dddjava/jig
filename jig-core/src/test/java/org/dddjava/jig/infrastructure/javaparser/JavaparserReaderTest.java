package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            """
    })
    @ParameterizedTest
    void JavadocコメントのないものはエラーにならずPackageCommentも生成されない(String code) {
        CompilationUnit cu = StaticJavaParser.parse(code);
        Optional<PackageComment> packageComment = sut.readPackageComment(cu);
        assertTrue(packageComment.isEmpty());
    }

    @Test
    void 概要のみのコメントが読み取れる() {
        String code = """
                /**
                 * packageにつけられたコメント
                 */
                package org.dddjava.jig.my_package;
                """;
        CompilationUnit cu = StaticJavaParser.parse(code);

        Optional<PackageComment> packageComment = sut.readPackageComment(cu);

        PackageComment actual = packageComment.orElseThrow();
        assertEquals("packageにつけられたコメント", actual.asText());
    }

    @Test
    void 概要と本文のコメントが読み取れる() {
        String code = """
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
                """;
        CompilationUnit cu = StaticJavaParser.parse(code);

        Optional<PackageComment> packageComment = sut.readPackageComment(cu);

        PackageComment actual = packageComment.orElseThrow();
        assertEquals("packageにつけられたコメント", actual.asText());
        assertEquals("""
                ここからが本文です。複文もOK
                
                改行されたものも入ります。
                末尾の改行は入りません。""", actual.descriptionComment().bodyText());
    }


}