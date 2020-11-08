package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;

/**
 * クラスに静的に属するもの
 *
 * コンストラクタはメンバではないが、実装上はstaticファクトリメソッドと同等の役割を担うのでここで扱う。
 */
public class JigTypeMember {
    JigMethods constructors;
    JigMethods staticMethods;
    StaticFieldDeclarations staticFieldDeclarations;

    public JigTypeMember(JigMethods constructors, JigMethods staticMethods, StaticFieldDeclarations staticFieldDeclarations) {
        this.constructors = constructors;
        this.staticMethods = staticMethods;
        this.staticFieldDeclarations = staticFieldDeclarations;
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return staticFieldDeclarations;
    }
}
