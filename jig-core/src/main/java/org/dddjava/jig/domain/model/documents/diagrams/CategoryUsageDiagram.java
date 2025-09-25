package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.StringJoiner;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram implements DiagramSourceWriter {

    private final ServiceMethods serviceMethods;
    private final JigTypes coreDomainJigTypes;
    private final TypeRelationships relationships;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, CoreTypesAndRelations coreTypesAndRelations) {
        this.serviceMethods = serviceMethods;
        this.coreDomainJigTypes = coreTypesAndRelations.coreJigTypes();
        this.relationships = coreTypesAndRelations.internalTypeRelationships();
    }

    public static Node usecase(ServiceMethod serviceMethod) {
        JigMethod jigMethod = serviceMethod.method();
        return new Node(jigMethod.jigMethodId().value())
                .shape("ellipse")
                .label(jigMethod.aliasText())
                .tooltip(jigMethod.simpleText())
                .as(NodeRole.準主役)
                .url(jigMethod.jigMethodDeclaration().declaringTypeId(), JigDocument.ApplicationSummary);
    }

    @Override
    public int write(Consumer<DiagramSource> diagramSourceWriteProcess) {
        JigTypes categoryJigTypes = coreDomainJigTypes.filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);

        if (categoryJigTypes.empty()) {
            return 0;
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
                useCaseText.add(usecase(serviceMethod).dotText());
            }
        }

        DocumentName documentName = DocumentName.of(JigDocument.CategoryUsageDiagram);
        var dotText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
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
                        .map(edge -> "\"%s\" -> \"%s\"".formatted(edge.from().fqn(), edge.to().fqn()))
                        .collect(joining("\n")))
                .add(serviceRelationText.dotText())
                .toString();

        diagramSourceWriteProcess.accept(DiagramSource.createDiagramSourceUnit(documentName, dotText));
        return 1;
    }

    private String nonCategoryNodeTexts(TypeIds categoryRelatedTypes) {
        return coreDomainJigTypes.orderedStream()
                .filter(jigType -> jigType.toValueKind() != JigTypeValueKind.区分)
                .filter(jigType -> categoryRelatedTypes.contains(jigType.id()))
                .map(jigType -> Node.businessRuleNodeOf(jigType))
                .map(Node::dotText)
                .collect(joining("\n"));
    }

    String categoryNodeTexts(JigTypes categoryJigTypes) {
        return categoryJigTypes.list().stream()
                .map(jigType -> Node.typeOf(jigType.id())
                        .label(jigType.term().titleAndSimpleName("\\n"))
                        .as(NodeRole.主役))
                .map(Node::dotText)
                .collect(joining("\n"));
    }
}
