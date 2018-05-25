package org.dddjava.jig.domain.model.implementation.bytecode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * モデルの実装一式
 */
public class ByteCodes {
    private final List<ByteCode> list;

    public ByteCodes(List<ByteCode> list) {
        this.list = list;
    }

    public List<ByteCode> list() {
        return list;
    }

    public List<MethodByteCode> instanceMethodSpecifications() {
        return list.stream()
                .map(ByteCode::instanceMethodSpecifications)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
