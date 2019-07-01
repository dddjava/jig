package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCode;

public interface IsBusinessRule {

    boolean isBusinessRule(TypeByteCode typeByteCode);
}
