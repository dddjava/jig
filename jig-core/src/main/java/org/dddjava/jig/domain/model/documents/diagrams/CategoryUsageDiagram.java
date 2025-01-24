package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.JigTypeValueKind;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.relation.ClassRelations;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram implements DiagramSourceWriter {

    ServiceMethods serviceMethods;
    private final JigTypes coreDomainJigTypes;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, JigTypes coreDomainJigTypes) {
        this.serviceMethods = serviceMethods;
        this.coreDomainJigTypes = coreDomainJigTypes;
    }

    public DiagramSources sources() {
        JigTypes categoryJigTypes = coreDomainJigTypes.filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);

        if (categoryJigTypes.empty()) {
            return DiagramSource.empty();
        }

        ClassRelations businessRuleRelations = ClassRelations.internalRelation(coreDomainJigTypes);
        ClassRelations relations = businessRuleRelations.relationsFromRootTo(categoryJigTypes.typeIdentifiers());
        TypeIdentifiers categoryRelatedTypes = relations.allTypeIdentifiers();

        StringJoiner useCaseText = new StringJoiner("\n");
        RelationText serviceRelationText = new RelationText();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            boolean related = false;
            TypeIdentifiers serviceMethodUsingTypes = serviceMethod.usingTypes();
            for (TypeIdentifier usingTypeIdentifier : serviceMethodUsingTypes.list()) {
                if (categoryRelatedTypes.contains(usingTypeIdentifier)
                        // ビジネスルールとの関連を持たないCategoryも対象にするためのor条件
                        || categoryJigTypes.typeIdentifiers().contains(usingTypeIdentifier)) {
                    // サービスメソッドからBusinessRule（Category含む）への関連を追加する
                    // この関連は[クラス->クラス]でなく[メソッド -> クラス]の関連になる
                    serviceRelationText.add(serviceMethod.methodDeclaration(), usingTypeIdentifier);
                    related = true;
                }
            }

            if (related) {
                // enumに関連しているサービスメソッドだけ出力する
                useCaseText.add(Nodes.usecase(serviceMethod).asText());
            }
        }

        DocumentName documentName = DocumentName.of(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,fillcolor=white];")
                .add("{")
                .add("rank=sink;")
                .add("node [shape=box3d];")
                .add(categoryNodeTexts(categoryJigTypes))
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(nonCategoryNodeTexts(categoryRelatedTypes))
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    private String nonCategoryNodeTexts(TypeIdentifiers categoryRelatedTypes) {
        return coreDomainJigTypes.stream()
                .filter(jigType -> jigType.toValueKind() != JigTypeValueKind.区分)
                .filter(jigType -> categoryRelatedTypes.contains(jigType.typeIdentifier()))
                .map(jigType -> Nodes.businessRuleNodeOf(jigType))
                .map(Node::asText)
                .collect(joining("\n"));
    }

    String categoryNodeTexts(JigTypes categoryJigTypes) {
        return categoryJigTypes.list().stream()
                .map(jigType -> Node.typeOf(jigType.typeIdentifier())
                        .label(jigType.nodeLabel())
                        .as(NodeRole.主役))
                .map(Node::asText)
                .collect(joining("\n"));
    }
}
