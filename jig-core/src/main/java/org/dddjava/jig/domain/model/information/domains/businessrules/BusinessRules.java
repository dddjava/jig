package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

/**
 * ビジネスルール一覧
 */
public class BusinessRules extends JigTypes {

    public BusinessRules(JigTypes jigTypes) {
        super(jigTypes.list());
    }
}
