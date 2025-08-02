package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 出力ポート／アダプタの実装群
 */
public class OutputImplementations {

    List<OutputImplementation> list;

    public OutputImplementations(List<OutputImplementation> list) {
        this.list = list;
    }

    public List<OutputImplementation> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public Gateways repositoryMethods() {
        return list.stream().map(OutputImplementation::outputPortGateway)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Gateways::new));
    }

    // FIXME これのテストがない
    public static OutputImplementations from(JigTypes jigTypes) {
        List<OutputImplementation> list = new ArrayList<>();
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
                            .map(implJigMethod -> new OutputImplementation(interfaceJigMethod, implJigMethod, interfaceJigType))
                            .forEach(outputImplementation -> list.add(outputImplementation));
                }
            }
        }
        return new OutputImplementations(list);
    }
}
