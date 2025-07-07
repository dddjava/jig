package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram implements DiagramSourceWriter {

    private final ServiceMethods serviceMethods;
    private final JigTypes coreDomainJigTypes;
    private final TypeRelationships relationships;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, JigTypesWithRelationships jigTypesWithRelationships) {
        this.serviceMethods = serviceMethods;
        this.coreDomainJigTypes = jigTypesWithRelationships.jigTypes();
        this.relationships = jigTypesWithRelationships.typeRelationships();
    }

    public DiagramSources sources() {
        JigTypes categoryJigTypes = coreDomainJigTypes.filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);

        if (categoryJigTypes.empty()) {
            return DiagramSource.empty();
        }

        TypeRelationships relations = relationships.relationsFromRootTo(categoryJigTypes.typeIds());
        TypeIds categoryRelatedTypes = relations.toTypeIds();

        StringJoiner useCaseText = new StringJoiner("\n");
        RelationText serviceRelationText = new RelationText();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            boolean related = false;
            TypeIds serviceMethodUsingTypes = serviceMethod.usingTypes();
            for (TypeId usingTypeId : serviceMethodUsingTypes.list()) {
                if (categoryRelatedTypes.contains(usingTypeId)
                        // ビジネスルールとの関連を持たないCategoryも対象にするためのor条件
                        || categoryJigTypes.typeIds().contains(usingTypeId)) {
                    // サービスメソッドからBusinessRule（Category含む）への関連を追加する
                    // この関連は[クラス->クラス]でなく[メソッド -> クラス]の関連になる
                    serviceRelationText.add(serviceMethod.method().jigMethodId(), usingTypeId);
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
                .add(relations.toEdges().orderedUniqueStream()
                        .map(edge -> "\"%s\" -> \"%s\"".formatted(edge.from().fullQualifiedName(), edge.to().fullQualifiedName()))
                        .collect(joining("\n")))
                .add(serviceRelationText.asText())
                .toString());
    }

    private String nonCategoryNodeTexts(TypeIds categoryRelatedTypes) {
        return coreDomainJigTypes.orderedStream()
                .filter(jigType -> jigType.toValueKind() != JigTypeValueKind.区分)
                .filter(jigType -> categoryRelatedTypes.contains(jigType.id()))
                .map(jigType -> Nodes.businessRuleNodeOf(jigType))
                .map(Node::asText)
                .collect(joining("\n"));
    }

    String categoryNodeTexts(JigTypes categoryJigTypes) {
        return categoryJigTypes.list().stream()
                .map(jigType -> Node.typeOf(jigType.id())
                        .label(jigType.term().titleAndSimpleName("\\n"))
                        .as(NodeRole.主役))
                .map(Node::asText)
                .collect(joining("\n"));
    }
}
