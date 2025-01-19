package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.List;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    JigTypes jigTypes;

    public BusinessRules(JigTypes jigTypes) {
        this.jigTypes = jigTypes;
    }

    public List<JigType> list() {
        return jigTypes.list();
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public JigTypes jigTypes() {
        return jigTypes.jigTypes();
    }
}
