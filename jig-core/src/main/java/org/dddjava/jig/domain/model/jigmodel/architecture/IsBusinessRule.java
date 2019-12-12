package org.dddjava.jig.domain.model.jigmodel.architecture;

import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;

public interface IsBusinessRule {

    boolean isBusinessRule(TypeByteCode typeByteCode);
}
