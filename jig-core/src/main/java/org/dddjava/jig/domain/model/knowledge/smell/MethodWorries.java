package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.information.type.JigType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの気になるところ
 */
public record MethodWorries(List<MethodWorry> list) {

    public static MethodWorries from(JigMethod method, JigType contextJigType) {
        return new MethodWorries(Arrays.stream(MethodWorry.values())
                .filter(methodWorry -> methodWorry.judge(method, contextJigType))
                .collect(Collectors.toList()));
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
