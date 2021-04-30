package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethods;
import org.dddjava.jig.domain.model.parts.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * クラスに静的に属するもの
 *
 * コンストラクタはメンバではないが、実装上はstaticファクトリメソッドと同等の役割を担うのでここで扱う。
 */
public class JigStaticMember {
    JigMethods constructors;
    JigMethods staticMethods;
    StaticFieldDeclarations staticFieldDeclarations;

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
}
