package org.dddjava.jig.infrastructure.javaparser.learning;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class JavaparserLineSeparatorTest {

    @Test
    void StaticJavaParserでparseすると改行コードがシステムのになる() {
        System.setProperty("line.separator", "\r\n");

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

        var comment = sut.getAllComments().get(0);

        // commentのStringは改行コード\nのまま
        String commentAsText = comment.asString();
        assertFalse(commentAsText.contains("\r\n"));

        // asJavadocCommentはthisかえしてるだけ
        JavadocComment javadocComment = comment.asJavadocComment();
        assertSame(comment, javadocComment);

        // これが戦犯
        Javadoc javadoc = javadocComment.parse();

        // javadoc.toText() の中で LineSeparator.SYSTEM を使って連結している。
        // ここでも入るが、javadocをつくるときにdescriptionの段階で \r\n は入っている。
        String javadocText = javadoc.toText();
        assertTrue(javadocText.contains("\r\n"));

        // javadoc.toText() のなかで description.toText() 呼んでるのでこれはこうなる。
        JavadocDescription description = javadoc.getDescription();

        String descriptionText = description.toText();
        assertTrue(descriptionText.contains("\r\n"));
    }

    @Test
    void JavadocParserでparseすると改行コードがシステムのになる() throws ClassNotFoundException {
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
