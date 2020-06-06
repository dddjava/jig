package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleRelation;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * ビジネスルールの関連
 */
public class BusinessRuleRelationDiagram {

    BusinessRules businessRules;
    ClassRelations classRelations;

    public BusinessRuleRelationDiagram(BusinessRules businessRules, ClassRelations classRelations) {
        this.businessRules = businessRules;
        this.classRelations = classRelations;
    }

    public DiagramSources relationDotText(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter packageIdentifierFormatter, AliasFinder aliasFinder) {

        if (businessRules.empty()) {
            return DiagramSource.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.BusinessRuleRelationDiagram);
        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        // nodes
        List<BusinessRulePackage> list = businessRules.businessRulePackages().list();

        for (BusinessRulePackage businessRulePackage : list) {
            PackageIdentifier packageIdentifier = businessRulePackage.packageIdentifier();

            Subgraph subgraph = new Subgraph(packageIdentifier.asText())
                    .label(packageIdentifier.format(packageIdentifierFormatter))
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            List<BusinessRule> businessRules = businessRulePackage.businessRules().list();
            for (BusinessRule businessRule : businessRules) {
                TypeAlias typeAlias = aliasFinder.find(businessRule.type().identifier());
                String aliasLine = "";
                if (typeAlias.exists()) {
                    aliasLine = typeAlias.asText() + "\n";
                }
                Node node = Node.controllerNodeOf(businessRule.type().identifier())
                        .label(aliasLine + businessRule.type().identifier().asSimpleText());

                node = (aliasLine.startsWith("*")) ? node.highlightColor() : node;

                subgraph.add(node.asText());
            }

            graph.add(subgraph.toString());
        }

        // relations
        List<BusinessRuleRelation> businessRuleRelations = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.list()) {
            if (businessRules.contains(classRelation.from()) && businessRules.contains(classRelation.to())) {
                businessRuleRelations.add(new BusinessRuleRelation(classRelation));
            }
        }

        RelationText relationText = new RelationText();
        for (BusinessRuleRelation relation : businessRuleRelations) {
            relationText.add(relation.from(), relation.to());
        }
        graph.add(relationText.asText());

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
