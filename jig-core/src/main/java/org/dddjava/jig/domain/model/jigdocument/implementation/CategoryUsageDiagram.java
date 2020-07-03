package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
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
    CategoryTypes categoryTypes;
    ClassRelations businessRuleRelations;

    public CategoryUsageDiagram(ServiceMethods serviceMethods, CategoryTypes categoryTypes, ClassRelations businessRuleRelations) {
        this.serviceMethods = serviceMethods;
        this.categoryTypes = categoryTypes;
        this.businessRuleRelations = businessRuleRelations;
    }

    public DiagramSources diagramSource(JigDocumentContext jigDocumentContext) {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        ClassRelations relations = businessRuleRelations.relationsFromRootTo(categoryTypes.typeIdentifiers());
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

            useCaseText.add(Nodes.usecase(jigDocumentContext, serviceMethod).asText());
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                //.add("node [shape=box,style=filled,fillcolor=lightgoldenrod];")
                .add("node [shape=box,style=filled,fillcolor=white];")
                .add("{")
                .add("rank=sink;")
                .add(categoryNodesText(jigDocumentContext))
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(exceptCategoryNodesText(jigDocumentContext, businessRuleTypeIdentifiers))
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    private String exceptCategoryNodesText(JigDocumentContext jigDocumentContext, TypeIdentifiers businessRuleTypeIdentifiers) {
        return businessRuleTypeIdentifiers
                .exclude(categoryTypes.typeIdentifiers())
                .list().stream()
                .map(typeIdentifier -> Node.controllerNodeOf(typeIdentifier)
                        .label(jigDocumentContext.aliasFinder().simpleTypeText(typeIdentifier))
                        .asText())
                .collect(Collectors.joining("\n"));
    }

    String categoryNodesText(JigDocumentContext jigDocumentContext) {
        return categoryTypes.list().stream()
                .map(categoryType -> categoryType.typeIdentifier())
                .map(typeIdentifier -> Node.controllerNodeOf(typeIdentifier)
                        .normalColor()
                        .label(jigDocumentContext.aliasFinder().typeText(typeIdentifier))
                        .asText())
                .collect(joining("\n"));
    }
}
