package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.FieldType;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.comment.Comment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JigExpressionObjectTest {

    @Test
    void 和名のあるドメイン型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        TypeIdentifier fieldTypeIdentifier = new TypeIdentifier("hoge.fuga.FieldType");
        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(fieldTypeIdentifier),
                "フィールド名");

        when(jigDocumentContext.classComment(any()))
                .thenReturn(new ClassComment(fieldTypeIdentifier, Comment.fromCodeComment("FieldType和名")));

        String actual = sut.fieldRawText(field);

        assertEquals("<a href=\"./domain.html#hoge.fuga.FieldType\">FieldType和名</a>", actual);
    }

    @Test
    void 和名のないドメイン型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class, in -> ClassComment.empty(in.getArgument(0)));
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        TypeIdentifier fieldTypeIdentifier = new TypeIdentifier("hoge.fuga.FieldType");
        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(fieldTypeIdentifier),
                "フィールド名");

        String actual = sut.fieldRawText(field);

        assertEquals("<a href=\"./domain.html#hoge.fuga.FieldType\">FieldType</a>", actual);
    }

    @Test
    void 非ジェネリクスJava標準型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class, in -> ClassComment.empty(in.getArgument(0)));
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(new TypeIdentifier(String.class)),
                "フィールド名");

        String actual = sut.fieldRawText(field);

        assertEquals("<span class=\"weak\">String</span>", actual);
    }

    @Test
    void ジェネリクスJava標準型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class, in -> ClassComment.empty(in.getArgument(0)));
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(
                        new TypeIdentifier(Map.class),
                        new TypeIdentifiers(List.of(
                                new TypeIdentifier(String.class),
                                new TypeIdentifier(BigDecimal.class)))),
                "フィールド名");

        String actual = sut.fieldRawText(field);

        assertEquals("<span class=\"weak\">Map</span>&lt;<span class=\"weak\">String</span>, <span class=\"weak\">BigDecimal</span>&gt;", actual);
    }

    @Test
    void ジェネリクスJava標準型_パラメタはドメイン型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class, in -> ClassComment.empty(in.getArgument(0)));
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        TypeIdentifier domainType = new TypeIdentifier("hoge.fuga.DomainType");
        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(
                        new TypeIdentifier(List.class),
                        new TypeIdentifiers(List.of(domainType))),
                "フィールド名");

        when(jigDocumentContext.classComment(domainType))
                .thenReturn(new ClassComment(domainType, Comment.fromCodeComment("DomainType和名")));

        String actual = sut.fieldRawText(field);

        assertEquals("<span class=\"weak\">List</span>&lt;" +
                        "<a href=\"./domain.html#hoge.fuga.DomainType\">DomainType和名</a>" +
                        "&gt;"
                , actual);
    }

    @Test
    void ジェネリクスドメイン型_パラメタもドメイン型() {
        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class, in -> ClassComment.empty(in.getArgument(0)));
        JigExpressionObject sut = new JigExpressionObject(jigDocumentContext);

        TypeIdentifier domainType = new TypeIdentifier("hoge.fuga.Fuga");
        FieldDeclaration field = new FieldDeclaration(
                new TypeIdentifier("hoge.fuga.DeclaringType"),
                new FieldType(
                        new TypeIdentifier("hoge.fuga.Hoge"),
                        new TypeIdentifiers(List.of(domainType))),
                "フィールド名");

        when(jigDocumentContext.classComment(domainType))
                .thenReturn(new ClassComment(domainType, Comment.fromCodeComment("ふが")));

        String actual = sut.fieldRawText(field);

        assertEquals("<a href=\"./domain.html#hoge.fuga.Hoge\">Hoge</a>&lt;" +
                        "<a href=\"./domain.html#hoge.fuga.Fuga\">ふが</a>" +
                        "&gt;"
                , actual);
    }
}