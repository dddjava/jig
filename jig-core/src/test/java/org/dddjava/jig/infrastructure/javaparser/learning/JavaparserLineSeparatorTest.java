package org.dddjava.jig.infrastructure.javaparser.learning;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.LineSeparator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("システム改行コードを置き換える前に JavaParser （の LineSeparator ）が使用されると変更できないため無効としておく")
public class JavaparserLineSeparatorTest {

    @Test
    void StaticJavaParserでparseすると改行コードがシステムのになる() {
        // システム改行コードを置き換え
        System.setProperty("line.separator", "\r\n");

        assertEquals("\r\n", LineSeparator.SYSTEM.asRawString(), "改行コードが置き換えられている");

        // この改行コードは \n （ファイルの改行コード）になる
        var code = """
                /**
                 * 複数行の
                 * Javadoc
                 * コメントだよ
                 */
                class JavadocClass {
                }
                """;
        CompilationUnit sut = StaticJavaParser.parse(code);

        Comment comment = sut.getAllComments().getFirst();

        // Comment の改行コードはファイルの改行コードのままになる
        // ファイルの改行コードに左右されることを認識しておく必要がある
        String commentAsText = comment.asString();
        assertFalse(commentAsText.contains("\r\n"), "Comment の改行コードにシステム改行コードは入っていない");

        // asJavadocComment は this を返してるだけなので変わらない
        JavadocComment javadocComment = comment.asJavadocComment();
        assertSame(comment, javadocComment);

        // JavadocComment -> Javadoc の変換
        // この中で JavadocParser を使ってパースしている。
        // JavadocParser はコメントをパースする際に改行コードを除去し、
        // description として再構築する際に LineSeparator.SYSTEM を使って連結している。
        // そのためこの時点でコードとして記述された改行コードは失われる。
        Javadoc javadoc = javadocComment.parse();

        String javadocText = javadoc.toText();
        assertTrue(javadocText.contains("\r\n"), "Javadoc.toText() の改行コードはシステム改行コードになっている");
    }

    @Test
    void JavadocParserでparseすると改行コードがシステムのになる() {
        System.setProperty("line.separator", "\r\n");

        String javadocCode = """
                /**
                 * this
                 * is
                 * javadoc
                 */
                """;
        var javadoc = StaticJavaParser.parseJavadoc(javadocCode);

        var descriptionText = javadoc.getDescription().toText();
        assertTrue(descriptionText.contains("\r\n"));
    }
}
