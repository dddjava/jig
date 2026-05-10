package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationKind;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalDependencyDiagramTest {

    private static TypeRelationship rel(String from, String to) {
        return new TypeRelationship(TypeId.valueOf(from), TypeId.valueOf(to), TypeRelationKind.使用アノテーション);
    }

    @Test
    void 同一外部グループへの複数参照が一意化される() {
        TypeRelationships relations = new TypeRelationships(List.of(
                rel("com.example.app.UserController", "org.springframework.web.bind.annotation.RestController"),
                rel("com.example.app.UserController", "org.springframework.web.servlet.ModelAndView"),
                rel("com.example.app.OrderController", "org.springframework.web.bind.annotation.GetMapping")
        ));

        ExternalDependencyDiagram diagram = ExternalDependencyDiagram.from(relations, ExternalGroupingRule.defaultRule());

        assertEquals(1, diagram.groups().size(), "spring-web に集約される想定");
        assertEquals("spring-web", diagram.groups().get(0).id());

        // エッジは (com.example.app -> spring-web) の1本に集約される
        assertEquals(1, diagram.edges().size());
        ExternalDependencyDiagram.Edge edge = diagram.edges().get(0);
        assertEquals("com.example.app", edge.from());
        assertEquals("spring-web", edge.to());
    }

    @Test
    void 複数の外部ライブラリと複数の解析対象パッケージが正しく集約される() {
        TypeRelationships relations = new TypeRelationships(List.of(
                rel("com.example.web.Controller", "org.springframework.web.bind.annotation.RestController"),
                rel("com.example.repo.UserMapper", "org.apache.ibatis.annotations.Mapper"),
                rel("com.example.repo.UserMapper", "java.util.List")
        ));

        ExternalDependencyDiagram diagram = ExternalDependencyDiagram.from(relations, ExternalGroupingRule.defaultRule());

        // groups: spring-web, mybatis, jdk
        assertEquals(3, diagram.groups().size());
        assertTrue(diagram.groups().stream().anyMatch(g -> g.id().equals("spring-web") && !g.isJdk()));
        assertTrue(diagram.groups().stream().anyMatch(g -> g.id().equals("mybatis") && !g.isJdk()));
        assertTrue(diagram.groups().stream().anyMatch(g -> g.id().equals("jdk") && g.isJdk()));

        // 解析対象パッケージは com.example.web と com.example.repo の2つ
        assertEquals(2, diagram.internalPackageFqns().size());
    }

    @Test
    void 配列型は要素型に正規化されJDKグループに集約される() {
        TypeRelationships relations = new TypeRelationships(List.of(
                rel("com.example.app.Service", "[Ljava.lang.String;"),
                rel("com.example.app.Service", "java.lang.String[]"),
                rel("com.example.app.Service", "int[]")
        ));

        ExternalDependencyDiagram diagram = ExternalDependencyDiagram.from(relations, ExternalGroupingRule.defaultRule());

        // すべて JDK に集約され、配列由来の `[Lcom...` のような外部グループが現れないこと
        assertEquals(1, diagram.groups().size());
        assertEquals("jdk", diagram.groups().get(0).id());
        assertTrue(diagram.groups().get(0).isJdk());
    }

    @Test
    void mermaidTextはincludeJdkでJDKグループの表示が切り替わる() {
        TypeRelationships relations = new TypeRelationships(List.of(
                rel("com.example.repo.UserMapper", "java.util.List"),
                rel("com.example.repo.UserMapper", "org.apache.ibatis.annotations.Mapper")
        ));
        ExternalDependencyDiagram diagram = ExternalDependencyDiagram.from(relations, ExternalGroupingRule.defaultRule());

        String withJdk = diagram.mermaidText(true);
        String withoutJdk = diagram.mermaidText(false);

        assertTrue(withJdk.contains("jdk"), "JDK 含む版にはJDKグループが含まれる");
        assertFalse(withoutJdk.contains("\"jdk\""), "JDK 含まない版にはJDKラベルが含まれない");
        assertTrue(withJdk.contains("mybatis"));
        assertTrue(withoutJdk.contains("mybatis"));
    }
}
