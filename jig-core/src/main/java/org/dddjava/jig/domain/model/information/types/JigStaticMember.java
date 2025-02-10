package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

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

    public JigStaticMember(JigMethods constructors, JigMethods staticMethods) {
        this.constructors = constructors;
        this.staticMethods = staticMethods;
    }

    List<TypeIdentifier> listUsingTypes() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(constructors.listUsingTypes());
        list.addAll(staticMethods.listUsingTypes());
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
