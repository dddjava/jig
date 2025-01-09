package org.dddjava.jig.domain.model.information.jigobject.class_;

import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * クラスに静的に属するもの
 *
 * コンストラクタはメンバではないが、実装上はstaticファクトリメソッドと同等の役割を担うのでここで扱う。
 */
public class JigStaticMember {
    private final JigMethods constructors;
    private final JigMethods staticMethods;
    private final StaticFieldDeclarations staticFieldDeclarations;

    public JigStaticMember(JigMethods constructors, JigMethods staticMethods, StaticFieldDeclarations staticFieldDeclarations) {
        this.constructors = constructors;
        this.staticMethods = staticMethods;
        this.staticFieldDeclarations = staticFieldDeclarations;
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return staticFieldDeclarations;
    }

    List<TypeIdentifier> listUsingTypes() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(constructors.listUsingTypes());
        list.addAll(staticMethods.listUsingTypes());
        list.addAll(staticFieldDeclarations.listTypeIdentifiers());
        return list;
    }

    public JigMethods staticMethods() {
        return staticMethods;
    }

    public Stream<JigMethod> jigMethodStream() {
        return Stream.concat(
                constructors.stream(),
                staticMethods.stream()
        );
    }
}
