package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.StringJoiner;

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

    public DiagramSources sources() {
        if (jigTypes.empty()) {
            return DiagramSources.empty();
        }

        String structureText = jigTypes.list().stream()
                .map(categoryType -> {
                    StringJoiner categoryValues = new StringJoiner("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>");

                    List<String> list = categoryType.jigTypeMembers().enumConstantNames();
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
                            .url(categoryType.id(), JigDocument.EnumSummary)
                            .as(NodeRole.主役);
                })
                .map(Node::asText)
                .collect(joining("\n"));

        DocumentName documentName = DocumentName.of(JigDocument.CategoryDiagram);
        return DiagramSource.createDiagramSource(
                documentName, new StringJoiner("\n", "graph \"" + documentName.label() + "\" {", "}")
                        .add("label=\"" + documentName.label() + "\";")
                        .add("layout=fdp;")
                        .add("rankdir=LR;")
                        .add(Node.DEFAULT)
                        .add(structureText)
                        .toString());
    }
}
