package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static ClassAndMethodComments merge(ClassAndMethodComments a, ClassAndMethodComments b) {
        return new ClassAndMethodComments(
                Stream.of(a.list, b.list).flatMap(Collection::stream).collect(Collectors.toList()),
                Stream.of(a.methodList, b.methodList).flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
