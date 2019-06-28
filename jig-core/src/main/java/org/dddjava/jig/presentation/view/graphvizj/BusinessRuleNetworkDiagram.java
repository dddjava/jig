package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.businessrules.*;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.fact.alias.AliasFinder;
import org.dddjava.jig.domain.model.fact.alias.TypeAlias;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.List;
import java.util.StringJoiner;

public class BusinessRuleNetworkDiagram implements DotTextEditor<BusinessRuleNetwork> {

    PackageIdentifierFormatter packageIdentifierFormatter;
    AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public BusinessRuleNetworkDiagram(PackageIdentifierFormatter packageIdentifierFormatter,
                                      AliasFinder aliasFinder) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.aliasFinder = aliasFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(BusinessRuleNetwork network) {
        String dotText = toDotText(network);
        return new DotTexts(dotText);
    }

    private String toDotText(BusinessRuleNetwork network) {
        StringJoiner graph = new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.BusinessRuleRelationDiagram) + "\";")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        // nodes
        List<BusinessRuleGroup> list = network.groups();
        for (BusinessRuleGroup businessRuleGroup : list) {
            PackageIdentifier packageIdentifier = businessRuleGroup.packageIdentifier();
            StringJoiner subgraph = new StringJoiner("\n", "subgraph \"cluster_" + packageIdentifier.asText() + "\" {", "}")
                    .add("label=\"" + packageIdentifier.format(packageIdentifierFormatter) + "\"");

            List<BusinessRule> businessRules = businessRuleGroup.businessRules().list();
            for (BusinessRule businessRule : businessRules) {
                TypeAlias typeAlias = aliasFinder.find(businessRule.type().identifier());
                String aliasLine = "";
                if (typeAlias.exists()) {
                    aliasLine = typeAlias.asText() + "\n";
                }
                Node node = Node.of(businessRule.type().identifier())
                        .label(aliasLine + businessRule.type().identifier().asSimpleText());
                subgraph.add(node.asText());
            }

            graph.add(subgraph.toString());
        }

        // relations
        BusinessRuleRelations relations = network.relations();
        RelationText relationText = new RelationText();
        for (BusinessRuleRelation relation : relations.list()) {
            relationText.add(relation.from(), relation.to());
        }
        graph.add(relationText.asText());

        return graph.toString();
    }
}
