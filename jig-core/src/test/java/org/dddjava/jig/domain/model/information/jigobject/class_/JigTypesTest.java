package org.dddjava.jig.domain.model.information.jigobject.class_;

import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.data.comment.Comment;
import org.dddjava.jig.domain.model.information.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigTypesTest {

    @Test
    void 空リストでインスタンス生成ができる() {
        Assertions.assertDoesNotThrow(() -> {
            new JigTypes(List.of());
        });
    }

    @Test
    void 一件のJigTypesが作れる() {
        var fqn = "hoge.fuga.Foo";
        var jigTypes = new JigTypes(List.of(
                createJigType(fqn)
        ));

        assertTrue(jigTypes.resolveJigType(TypeIdentifier.valueOf(fqn)).isPresent());
    }

    @Test
    void 二件のJigTypesが作れる() {
        var fqn = "hoge.fuga.Foo";
        var jigTypes = new JigTypes(List.of(
                createJigType(fqn),
                createJigType("hoge.fuga.Bar")
        ));

        assertTrue(jigTypes.resolveJigType(TypeIdentifier.valueOf(fqn)).isPresent());
    }

    @Test
    void 同じのFQNのJigTypeでJigTypesが作れる() {
        assertDoesNotThrow(() -> {
            new JigTypes(List.of(
                    createJigType("hoge.fuga.Foo"),
                    createJigType("hoge.fuga.Foo")
            ));
        });
    }

    private static JigType createJigType(String fqn) {
        var typeIdentifier = TypeIdentifier.valueOf(fqn);
        return new JigType(
                new TypeDeclaration(
                        new ParameterizedType(typeIdentifier),
                        new ParameterizedType(TypeIdentifier.from(Object.class)),
                        new ParameterizedTypes(List.of())),
                new JigTypeAttribute(
                        new ClassComment(
                                typeIdentifier,
                                Comment.empty()
                        ),
                        TypeKind.通常型,
                        TypeVisibility.PUBLIC,
                        List.of()
                ),
                new JigStaticMember(
                        new JigMethods(List.of()),
                        new JigMethods(List.of()),
                        new StaticFieldDeclarations(List.of())
                ),
                new JigInstanceMember(
                        new JigFields(List.of()),
                        new JigMethods(List.of())
                )
        );
    }
}