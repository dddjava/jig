package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの気になるところ
 */
public class MethodWorries {
    List<MethodWorry> list;

    public MethodWorries(JigMethod method) {
        this.list = Arrays.stream(MethodWorry.values())
                .filter(methodWorry -> methodWorry.judge(method))
                .collect(Collectors.toList());
    }

    public boolean contains(MethodWorry... methodWorries) {
        for (MethodWorry methodWorry : methodWorries) {
            if (list.contains(methodWorry)) return true;
        }
        return false;
    }
}
