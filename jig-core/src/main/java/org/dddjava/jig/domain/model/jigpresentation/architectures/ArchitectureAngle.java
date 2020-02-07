package org.dddjava.jig.domain.model.jigpresentation.architectures;

import org.dddjava.jig.application.repository.ArchitectureFactory;
import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.Node;
import org.dddjava.jig.domain.model.jigdocument.RelationText;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.relation.RoundingPackageRelations;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.StringJoiner;

/**
 * アーキテクチャの切り口
 */
public class ArchitectureAngle {

    AnalyzedImplementation analyzedImplementation;
    ArchitectureFactory architectureFactory;

    public ArchitectureAngle(AnalyzedImplementation analyzedImplementation, ArchitectureFactory architectureFactory) {
        this.analyzedImplementation = analyzedImplementation;
        this.architectureFactory = architectureFactory;
    }

    public DotText dotText(JigDocumentContext jigDocumentContext) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();

        Architecture architecture = architectureFactory.architecture();
        RoundingPackageRelations architectureRelation = architecture.toRoundingPackageRelations(typeByteCodes);

        if (architectureRelation.worthless()) {
            return DotText.empty();
        }

        StringJoiner graph = new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.ArchitectureDiagram) + "\";")
                .add(Node.DEFAULT);
        RelationText relationText = architectureRelation.toRelationText();
        graph.add(relationText.asText());
        return new DotText(graph.toString());
    }
}
