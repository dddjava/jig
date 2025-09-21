package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.List;

interface MethodSignatureData {

    JigTypeReference returnType();

    List<JigTypeReference> parameterTypeList();
}
