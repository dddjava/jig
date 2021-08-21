package org.dddjava.jig.domain.model.models.applications.backends;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データソースメソッド一覧
 */
public class DatasourceMethods {

    List<DatasourceMethod> list;

    public DatasourceMethods(List<DatasourceMethod> list) {
        this.list = list;
    }

    public List<DatasourceMethod> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public RepositoryMethods repositoryMethods() {
        return list.stream().map(DatasourceMethod::repositoryMethod)
                .collect(Collectors.collectingAndThen(Collectors.toList(), RepositoryMethods::new));
    }

    public static DatasourceMethods from(JigTypes jigTypes) {
        List<DatasourceMethod> list = new ArrayList<>();
        TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
        // backend実装となる@RepositoryのついているJigTypeを抽出
        for (JigType implJigType : jigTypes.listMatches(jigType -> jigType.hasAnnotation(repositoryAnnotation))) {
            // インタフェースを抽出（通常1件）
            for (JigType interfaceJigType : jigTypes.listMatches(item -> implJigType.typeDeclaration().interfaceTypes().listTypeIdentifiers().contains(item.identifier()))) {
                for (JigMethod interfaceJigMethod : interfaceJigType.instanceMember().instanceMethods().list()) {
                    implJigType.instanceMember().instanceMethods().list().stream()
                            // シグネチャが一致するもの
                            .filter(implJigMethod -> interfaceJigMethod.declaration().methodSignature().isSame(implJigMethod.declaration().methodSignature()))
                            .map(implJigMethod -> new DatasourceMethod(interfaceJigMethod, implJigMethod))
                            .forEach(datasourceMethod -> list.add(datasourceMethod));
                }
            }
        }
        return new DatasourceMethods(list);
    }
}
