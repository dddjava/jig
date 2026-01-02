package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSource;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.documents.stationery.Node;
import org.dddjava.jig.domain.model.documents.stationery.NodeRole;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

/**
 * 区分図
 */
public class CategoryDiagram implements DiagramSourceWriter {

    private final JigTypes jigTypes;

    CategoryDiagram(JigTypes jigTypes) {
        this.jigTypes = jigTypes;
    }

    public static CategoryDiagram create(JigTypes jigTypes) {
        return new CategoryDiagram(jigTypes);
    }

    @Override
    public int write(Consumer<DiagramSource> diagramSourceWriteProcess) {
        if (jigTypes.empty()) {
            return 0;
        }

        String structureText = jigTypes.list().stream()
                .map(categoryType -> {
                    StringJoiner categoryValues = new StringJoiner("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>");

                    List<String> list = categoryType.jigTypeMembers().enumConstantStream()
                            .map(jigField -> jigField.jigFieldHeader().name()).toList();
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 20) {
                            categoryValues.add("... more");
                            break;
                        }
                        String nameText = list.get(i);
                        categoryValues.add(nameText);
                    }
                    String categoryName = categoryType.label();

                    return Node.typeOf(categoryType.id())
                            .html(categoryName, "<table border=\"0\" cellspacing=\"0\"><tr><td>" + categoryName + "</td></tr>" + categoryValues + "</table>")
                            .url(categoryType.id(), JigDocument.DomainSummary)
                            .as(NodeRole.主役);
                })
                .map(Node::dotText)
                .collect(joining("\n"));

        DocumentName documentName = DocumentName.of(JigDocument.CategoryDiagram);
        var dotText = new StringJoiner("\n", "graph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("layout=fdp;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(structureText)
                .toString();

        diagramSourceWriteProcess.accept(DiagramSource.createDiagramSourceUnit(documentName, dotText));
        return 1;
    }
}
