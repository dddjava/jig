package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;

/**
 * クラスメンバ
 *
 * コンストラクタはメンバではないが、実装上はstaticファクトリメソッドと同等の役割を担うのでここで扱う。
 */
public class JigTypeMember {
    MethodDeclarations constructorDeclarations;
    MethodDeclarations staticMethodDeclarations;
    StaticFieldDeclarations staticFieldDeclarations;

    public JigTypeMember(MethodDeclarations constructorDeclarations, MethodDeclarations staticMethodDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        this.constructorDeclarations = constructorDeclarations;
        this.staticMethodDeclarations = staticMethodDeclarations;
        this.staticFieldDeclarations = staticFieldDeclarations;
    }
}
