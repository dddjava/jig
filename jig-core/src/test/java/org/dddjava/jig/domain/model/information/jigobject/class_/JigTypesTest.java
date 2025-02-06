package org.dddjava.jig.domain.model.information.jigobject.class_;

import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.term.TermKind;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.*;
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
                callerMethodsOfJigType(fqn)
        ));

        assertTrue(jigTypes.resolveJigType(TypeIdentifier.valueOf(fqn)).isPresent());
    }

    @Test
    void 二件のJigTypesが作れる() {
        var fqn = "hoge.fuga.Foo";
        var jigTypes = new JigTypes(List.of(
                callerMethodsOfJigType(fqn),
                callerMethodsOfJigType("hoge.fuga.Bar")
        ));

        assertTrue(jigTypes.resolveJigType(TypeIdentifier.valueOf(fqn)).isPresent());
    }

    @Test
    void 同じのFQNのJigTypeでJigTypesが作れる() {
        assertDoesNotThrow(() -> {
            new JigTypes(List.of(
                    callerMethodsOfJigType("hoge.fuga.Foo"),
                    callerMethodsOfJigType("hoge.fuga.Foo")
            ));
        });
    }

    private static JigType callerMethodsOfJigType(String fqn) {
        return JigType.from(
                JigTypeHeader.simple(fqn),
                new JigStaticMember(
                        new JigMethods(List.of()),
                        new JigMethods(List.of()),
                        new StaticFieldDeclarations(List.of())
                ),
                new JigInstanceMember(
                        new JigFields(List.of()),
                        new JigMethods(List.of())
                ),
                new JigTypeTerms(new Term(new TermIdentifier(""), "", "", TermKind.クラス), List.of())
        );
    }
}