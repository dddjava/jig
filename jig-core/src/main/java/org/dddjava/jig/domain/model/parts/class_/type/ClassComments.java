package org.dddjava.jig.domain.model.parts.class_.type;

import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;

import java.util.Collections;
import java.util.List;

/**
 * 型名一覧
 */
public class ClassComments {

    List<ClassComment> list;
    List<MethodComment> methodList;

    public ClassComments(List<ClassComment> list, List<MethodComment> methodList) {
        this.list = list;
        this.methodList = methodList;
    }

    public static ClassComments empty() {
        return new ClassComments(Collections.emptyList(), Collections.emptyList());
    }

    public List<ClassComment> list() {
        return list;
    }

    public List<MethodComment> methodList() {
        return methodList;
    }
}
