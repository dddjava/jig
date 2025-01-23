package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.data.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.data.jigobject.class_.TypeCategory;
import org.dddjava.jig.domain.model.data.jigobject.member.JigMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データソースメソッド一覧
 *
 * TODO Datasourceといいながら扱っているのはRepository全般っぽい？データソースとよんだりリポジトリと呼んだりの混乱がある
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
        // backend実装となる@RepositoryのついているJigTypeを抽出
        for (JigType implJigType : jigTypes.listMatches(jigType -> jigType.typeCategory() == TypeCategory.OutputAdapter)) {
            // インタフェースを抽出（通常1件）
            for (JigType interfaceJigType : jigTypes.listMatches(item -> implJigType.typeDeclaration().interfaceTypes().listTypeIdentifiers().contains(item.identifier()))) {
                for (JigMethod interfaceJigMethod : interfaceJigType.instanceMember().instanceMethods().list()) {
                    implJigType.instanceMember().instanceMethods().stream()
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
