package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import stub.domain.model.category.SimpleEnum;
import stub.domain.model.relation.clz.SuperClass;
import stub.domain.model.type.fuga.Fuga;
import testing.TestSupport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsightsTest {

    @Test
    void クラスを直接持たない中間パッケージもルートパッケージ以下なら一覧に含める() {
        var sut = insightsOf(SimpleEnum.class, SuperClass.class, Fuga.class);

        assertEquals(List.of(
                        "stub.domain.model",
                        "stub.domain.model.category",
                        "stub.domain.model.relation",
                        "stub.domain.model.relation.clz",
                        "stub.domain.model.type",
                        "stub.domain.model.type.fuga"),
                sut.packageInsightList().stream().map(PackageInsight::fqn).toList());
    }

    @Test
    void 補った中間パッケージはクラスを持たない() {
        var sut = insightsOf(SimpleEnum.class, SuperClass.class);

        var model = sut.packageInsightList().stream()
                .filter(packageInsight -> packageInsight.fqn().equals("stub.domain.model.relation"))
                .findFirst().orElseThrow();
        assertEquals(0, model.numberOfTypes());
        assertEquals(0, model.size());
    }

    private static Insights insightsOf(Class<?>... classes) {
        JigTypes jigTypes = TestSupport.buildJigTypes(classes);
        var methodInsights = jigTypes.orderedStream()
                .flatMap(JigType::allJigMethodStream)
                .map(MethodInsight::new)
                .toList();
        return new Insights(methodInsights, new Glossary(List.of()), TypeRelationships.internalRelation(jigTypes), jigTypes);
    }
}
