package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.comment.Comment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LabelerTest {

    @Test
    public void コメントなしのラベル() {
        PackageIdentifier identifier = new PackageIdentifier("package");

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, Comment.empty()));

        Labeler labeler = new Labeler(context);

        String label = labeler.label(identifier);
        assertEquals("package", label);
    }

    @Test
    public void コメントありのラベル() {
        PackageIdentifier identifier = new PackageIdentifier("package");

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, Comment.fromCodeComment("コメント")));

        Labeler labeler = new Labeler(context);

        String label = labeler.label(identifier);
        assertEquals("コメント\\npackage", label);
    }

    @Test
    public void parentの部分が除去される() {
        PackageIdentifier parent = new PackageIdentifier("grandparent.parent");
        PackageIdentifier identifier = new PackageIdentifier("grandparent.parent.child");

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(parent, Comment.empty()));
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, Comment.empty()));

        Labeler labeler = new Labeler(context);

        String label = labeler.label(identifier, parent);
        assertEquals("child", label);
    }

    @Test
    public void parentの部分が除去される_途中まででも() {
        PackageIdentifier parent = new PackageIdentifier("grandparent.parent");
        PackageIdentifier identifier = new PackageIdentifier("grandparent.parent.child.hoge");

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(parent, Comment.empty()));
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, Comment.empty()));

        Labeler labeler = new Labeler(context);

        String label = labeler.label(identifier, parent);
        assertEquals("child.hoge", label);
    }

    @Test
    public void parentと重複していなかったら除去されない_この時点で異常系だけど() {
        PackageIdentifier parent = new PackageIdentifier("grandparent.parent");
        PackageIdentifier identifier = new PackageIdentifier("grandparent.child");

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(parent, Comment.empty()));
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, Comment.empty()));

        Labeler labeler = new Labeler(context);

        String label = labeler.label(identifier, parent);
        assertEquals("grandparent.child", label);
    }

    @Test
    void applyContext() {
        JigDocumentContext context = mock(JigDocumentContext.class);
        Labeler labeler = new Labeler(context);

        List<PackageIdentifier> groupingPackage = Collections.singletonList(new PackageIdentifier("org.test.hoge"));
        List<PackageIdentifier> standalonePackage = Collections.singletonList(new PackageIdentifier("org.test.standalone"));

        labeler.applyContext(groupingPackage, standalonePackage);
        assertEquals("root: org.test", labeler.contextDescription());
    }
}