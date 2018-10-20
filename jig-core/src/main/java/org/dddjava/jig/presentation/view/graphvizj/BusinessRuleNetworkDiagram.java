package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.networks.businessrule.BusinessRuleGroup;
import org.dddjava.jig.domain.model.networks.businessrule.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.networks.businessrule.BusinessRuleRelation;
import org.dddjava.jig.domain.model.networks.businessrule.BusinessRuleRelations;

import java.util.List;
import java.util.StringJoiner;

public class BusinessRuleNetworkDiagram implements DotTextEditor<BusinessRuleNetwork> {

    PackageIdentifierFormatter packageIdentifierFormatter;
    JapaneseNameFinder japaneseNameFinder;

    public BusinessRuleNetworkDiagram(PackageIdentifierFormatter packageIdentifierFormatter,
                                      JapaneseNameFinder japaneseNameFinder) {
        this.packageIdentifierFormatter = packageIdentifierFormatter;
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public DotTexts edit(BusinessRuleNetwork network) {
        String dotText = toDotText(network);
        return new DotTexts(dotText);
    }

    private String toDotText(BusinessRuleNetwork network) {
        StringJoiner graph = new StringJoiner("\n", "digraph {", "}")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        // nodes
        List<BusinessRuleGroup> list = network.aggregates();
        for (BusinessRuleGroup businessRuleGroup : list) {
            PackageIdentifier packageIdentifier = businessRuleGroup.packageIdentifier();
            StringJoiner subgraph = new StringJoiner("\n", "subgraph \"cluster_" + packageIdentifier.asText() + "\" {", "}")
                    .add("label=\"" + packageIdentifier.format(packageIdentifierFormatter) + "\"");

            List<BusinessRule> businessRules = businessRuleGroup.listBusinessRule();
            for (BusinessRule businessRule : businessRules) {
                Node node = Node.of(businessRule.type().identifier())
                        .label(businessRule.type().identifier().asSimpleText());
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
