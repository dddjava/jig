package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram implements DiagramSourceWriter {

    ServiceMethods serviceMethods;
    CategoryTypes categoryTypes;
    private final JigTypes coreDomainJigTypes;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, CategoryTypes categoryTypes, JigTypes coreDomainJigTypes) {
        this.serviceMethods = serviceMethods;
        this.categoryTypes = categoryTypes;
        this.coreDomainJigTypes = coreDomainJigTypes;
    }

    public DiagramSources sources() {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        ClassRelations businessRuleRelations = coreDomainJigTypes.internalClassRelations();
        ClassRelations relations = businessRuleRelations.relationsFromRootTo(categoryTypes.typeIdentifiers());
        TypeIdentifiers categoryRelatedTypes = relations.allTypeIdentifiers();

        StringJoiner useCaseText = new StringJoiner("\n");
        RelationText serviceRelationText = new RelationText();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            boolean related = false;
            TypeIdentifiers serviceMethodUsingTypes = serviceMethod.usingTypes();
            for (TypeIdentifier usingTypeIdentifier : serviceMethodUsingTypes.list()) {
                if (categoryRelatedTypes.contains(usingTypeIdentifier)
                        // ビジネスルールとの関連を持たないCategoryも対象にするためのor条件
                        || categoryTypes.typeIdentifiers().contains(usingTypeIdentifier)) {
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
                .add(categoryNodeTexts())
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

    String categoryNodeTexts() {
        return categoryTypes.list().stream()
                .map(CategoryUsageDiagram::getNode)
                .map(Node::asText)
                .collect(joining("\n"));
    }

    private static Node getNode(CategoryType categoryType) {
        Node node = new Node(categoryType.typeIdentifier().fullQualifiedName()).label(categoryType.nodeLabel());
        return node.as(categoryType.hasBehaviour() ? NodeRole.主役 : NodeRole.準主役);
    }
}
