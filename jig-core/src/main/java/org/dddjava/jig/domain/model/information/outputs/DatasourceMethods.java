package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

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
                for (JigMethod interfaceJigMethod : interfaceJigType.instanceJigMethods().list()) {
                    implJigType.instanceJigMethodStream()
                            // 名前と引数型が一致するもの
                            .filter(implJigMethod -> interfaceJigMethod.jigMethodId().name().equals(implJigMethod.jigMethodId().name()))
                            .filter(implJigMethod -> interfaceJigMethod.jigMethodId().tuple().parameterTypeNameList().equals(implJigMethod.jigMethodId().tuple().parameterTypeNameList()))
                            .map(implJigMethod -> new DatasourceMethod(interfaceJigMethod, implJigMethod, interfaceJigType))
                            .forEach(datasourceMethod -> list.add(datasourceMethod));
                }
            }
        }
        return new DatasourceMethods(list);
    }
}
