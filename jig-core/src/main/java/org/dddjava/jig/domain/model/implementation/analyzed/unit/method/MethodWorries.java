package org.dddjava.jig.domain.model.implementation.analyzed.unit.method;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.MethodByteCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの気になるところ
 */
public class MethodWorries {
    List<MethodWorry> list;

    MethodWorries(List<MethodWorry> list) {
        this.list = list;
    }

    public static MethodWorries from(MethodByteCode methodByteCode) {
        List<MethodWorry> list = Arrays.stream(MethodWorry.values())
                .filter(methodWorry -> methodWorry.judge(methodByteCode))
                .collect(Collectors.toList());
        return new MethodWorries(list);
    }

    public boolean contains(MethodWorry methodWorry) {
        return list.contains(methodWorry);
    }
}
