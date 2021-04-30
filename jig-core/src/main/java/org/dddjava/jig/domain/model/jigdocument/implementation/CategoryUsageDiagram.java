package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.categories.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifiers;

import java.util.StringJoiner;

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
        this.categoryTypes = CategoryTypes.from(businessRules.jigTypes());
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
                .add("node [shape=box3d];")
                .add(categoryNodeTexts())
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(nonCategoryBusinessRuleNodeTexts(categoryRelatedTypes))
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    private String nonCategoryBusinessRuleNodeTexts(TypeIdentifiers categoryRelatedTypes) {
        return businessRules.list().stream()
                .filter(businessRule -> businessRule.toValueKind() != JigTypeValueKind.区分)
                .filter(businessRule -> categoryRelatedTypes.contains(businessRule.typeIdentifier()))
                .map(businessRule -> Node.businessRuleNodeOf(businessRule))
                .map(Node::asText)
                .collect(joining("\n"));
    }

    String categoryNodeTexts() {
        return categoryTypes.list().stream()
                .map(Node::categoryNodeOf)
                .map(Node::asText)
                .collect(joining("\n"));
    }
}
