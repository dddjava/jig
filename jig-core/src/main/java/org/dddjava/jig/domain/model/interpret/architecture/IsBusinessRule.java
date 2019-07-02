package org.dddjava.jig.domain.model.interpret.architecture;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

public interface IsBusinessRule {

    boolean isBusinessRule(TypeByteCode typeByteCode);
}
