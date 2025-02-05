package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.Comment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LabelerTest {

    static Stream<Arguments> testLabeler() {
        return Stream.of(
                Arguments.of("root.package", "root", Comment.empty(), "package"),
                Arguments.of("root.package", "root", Comment.fromCodeComment("コメント"), "コメント\\npackage"),
                Arguments.of("grandparent.parent.child", "grandparent.parent", Comment.empty(), "child"),
                Arguments.of("grandparent.parent.child.hoge", "grandparent.parent", Comment.empty(), "child.hoge"),
                Arguments.of("grandparent.child", "grandparent.parent", Comment.empty(), "grandparent.child")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testLabeler(String identifierText, String parentText, Comment comment, String expectedLabel) {
        PackageIdentifier identifier = PackageIdentifier.valueOf(identifierText);
        PackageIdentifier parent = PackageIdentifier.valueOf(parentText);

        JigDocumentContext context = mock(JigDocumentContext.class);
        when(context.packageComment(any())).thenReturn(new PackageComment(identifier, comment));

        Labeler labeler = new Labeler(context);

        String actualLabel = labeler.label(identifier, parent);

        assertEquals(expectedLabel, actualLabel);
    }

    @Test
    void applyContext() {
        JigDocumentContext context = mock(JigDocumentContext.class);
        Labeler labeler = new Labeler(context);

        List<PackageIdentifier> groupingPackage = Collections.singletonList(PackageIdentifier.valueOf("org.test.hoge"));
        List<PackageIdentifier> standalonePackage = Collections.singletonList(PackageIdentifier.valueOf("org.test.standalone"));

        labeler.applyContext(groupingPackage, standalonePackage);
        assertEquals("root: org.test", labeler.contextDescription());
    }
}