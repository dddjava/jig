package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    void コメントのないものはエラーにならずPackageCommentも生成されない() {
        String code = """
                package org.dddjava.jig.my_package;
                """;
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
    void javadocコメントでないものはスルーされる() {
        String code = """
                /*
                 * none-javadocコメント
                 */
                package org.dddjava.jig.my_package;
                """;
        CompilationUnit cu = StaticJavaParser.parse(code);

        Optional<PackageComment> packageComment = sut.readPackageComment(cu);

        assertTrue(packageComment.isEmpty());
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