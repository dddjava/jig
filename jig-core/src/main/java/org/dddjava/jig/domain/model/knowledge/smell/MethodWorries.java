package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Arrays;
import java.util.List;

/**
 * メソッドの気になるところ
 */
record MethodWorries(List<MethodWorry> list) {

    public static MethodWorries from(JigMethod method, JigType contextJigType) {
        return new MethodWorries(Arrays.stream(MethodWorry.values())
                .filter(methodWorry -> methodWorry.judge(method, contextJigType))
                .toList());
    }

    public boolean contains(MethodWorry... methodWorries) {
        for (MethodWorry methodWorry : methodWorries) {
            if (list.contains(methodWorry)) return true;
        }
        return false;
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
