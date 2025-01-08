package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.data.classes.method.MethodComment;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;

import java.util.Collections;
import java.util.List;

/**
 * コメント一覧
 */
public class ClassAndMethodComments {

    List<ClassComment> list;
    List<MethodComment> methodList;

    public ClassAndMethodComments(List<ClassComment> list, List<MethodComment> methodList) {
        this.list = list;
        this.methodList = methodList;
    }

    public static ClassAndMethodComments empty() {
        return new ClassAndMethodComments(Collections.emptyList(), Collections.emptyList());
    }

    public List<ClassComment> list() {
        return list;
    }

    public List<MethodComment> methodList() {
        return methodList;
    }
}
