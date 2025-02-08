package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.method.JigMethods;

/**
 * インスタンスに属するもの
 */
public class JigInstanceMember {
    private final JigMethods instanceMethods;

    public JigInstanceMember(JigMethods instanceMethods) {
        this.instanceMethods = instanceMethods;
    }

    public JigMethods instanceMethods() {
        return instanceMethods;
    }
}
