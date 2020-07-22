package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;

import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram {

    ServiceMethods serviceMethods;
    BusinessRules businessRules;
    CategoryTypes categoryTypes;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, BusinessRules businessRules) {
        this.serviceMethods = serviceMethods;
        this.businessRules = businessRules;
        this.categoryTypes = businessRules.createCategoryTypes();
    }

    public DiagramSources diagramSource(JigDocumentContext jigDocumentContext) {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        ClassRelations businessRuleRelations = businessRules.businessRuleRelations();
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
                useCaseText.add(Nodes.usecase(jigDocumentContext, serviceMethod).asText());
            }
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                //.add("node [shape=box,style=filled,fillcolor=lightgoldenrod];")
                .add("node [shape=box,style=filled,fillcolor=white];")
                .add("{")
                .add("rank=sink;")
                .add(categoryNodesText())
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(exceptCategoryNodesText(jigDocumentContext, categoryRelatedTypes))
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    private String exceptCategoryNodesText(JigDocumentContext jigDocumentContext, TypeIdentifiers businessRuleTypeIdentifiers) {
        return businessRuleTypeIdentifiers
                .exclude(categoryTypes.typeIdentifiers())
                .list()
                .stream()
                .map(typeIdentifier -> Node.typeOf(typeIdentifier)
                        .label(jigDocumentContext.aliasFinder().simpleTypeText(typeIdentifier))
                        .asText())
                .collect(Collectors.joining("\n"));
    }

    String categoryNodesText() {
        return categoryTypes.list().stream()
                .map(Node::categoryNodeOf)
                .map(Node::asText)
                .collect(joining("\n"));
    }
}
