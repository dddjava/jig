package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeCategory;

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

    // FIXME これのテストがない
    public static DatasourceMethods from(JigTypes jigTypes) {
        List<DatasourceMethod> list = new ArrayList<>();
        // backend実装となる@RepositoryのついているJigTypeを抽出
        for (JigType implJigType : jigTypes.listMatches(jigType -> jigType.typeCategory() == TypeCategory.OutputAdapter)) {
            // インタフェースを抽出（通常1件）
            for (JigType interfaceJigType : jigTypes
                    .listMatches(item -> implJigType.jigTypeHeader().baseTypeDataBundle().interfaceTypes().stream()
                            .anyMatch(jigBaseTypeData -> jigBaseTypeData.id().equals(item.id())))) {
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
