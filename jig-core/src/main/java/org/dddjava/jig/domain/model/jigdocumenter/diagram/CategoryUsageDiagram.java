package org.dddjava.jig.domain.model.jigdocumenter.diagram;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocumenter.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram {

    ServiceMethods serviceMethods;
    CategoryTypes categoryTypes;
    ClassRelations classRelations;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, CategoryTypes categoryTypes, ClassRelations classRelations) {
        this.serviceMethods = serviceMethods;
        this.categoryTypes = categoryTypes;
        this.classRelations = classRelations;
    }

    public DiagramSources diagramSource(AliasFinder aliasFinder, JigDocumentContext jigDocumentContext) {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        ClassRelations relations = classRelations.relationsFromRootTo(categoryTypes.typeIdentifiers());
        TypeIdentifiers businessRuleTypeIdentifiers = relations.allTypeIdentifiers();

        StringJoiner useCaseText = new StringJoiner("\n");
        RelationText serviceRelationText = new RelationText();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            boolean relateService = false;
            TypeIdentifiers serviceUsingTypes = serviceMethod.usingTypes();
            for (TypeIdentifier usingTypeIdentifier : serviceUsingTypes.list()) {
                if (businessRuleTypeIdentifiers.contains(usingTypeIdentifier)
                        || categoryTypes.typeIdentifiers().contains(usingTypeIdentifier)) {
                    serviceRelationText.add(serviceMethod.methodDeclaration(), usingTypeIdentifier);
                    relateService = true;
                }
            }
            if (!relateService) {
                // enumから関連していないのは出力しない
                continue;
            }

            Node node = Node.of(serviceMethod.methodDeclaration())
                    .label(aliasFinder.methodText(serviceMethod.methodDeclaration().identifier()))
                    .normalColor()
                    .useCase();
            useCaseText.add(node.asText());
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                //.add("node [shape=box,style=filled,fillcolor=lightgoldenrod];")
                .add("node [shape=box,style=filled,fillcolor=white];")
                .add("{")
                .add("rank=sink;")
                .add(categoryNodesText(aliasFinder))
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(exceptCategoryNodesText(aliasFinder, businessRuleTypeIdentifiers))
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    private String exceptCategoryNodesText(AliasFinder aliasFinder, TypeIdentifiers businessRuleTypeIdentifiers) {
        return businessRuleTypeIdentifiers
                .exclude(categoryTypes.typeIdentifiers())
                .list().stream()
                .map(typeIdentifier -> Node.of(typeIdentifier)
                        .label(aliasFinder.simpleTypeText(typeIdentifier))
                        .asText())
                .collect(Collectors.joining("\n"));
    }

    String categoryNodesText(AliasFinder aliasFinder) {
        return categoryTypes.list().stream()
                .map(categoryType -> categoryType.typeIdentifier())
                .map(typeIdentifier -> Node.of(typeIdentifier)
                        .normalColor()
                        .label(aliasFinder.typeText(typeIdentifier))
                        .asText())
                .collect(joining("\n"));
    }
}
