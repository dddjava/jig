package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeValueKind;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;

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
        return new CategoryDiagram(jigTypes.filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分));
    }

    public DiagramSources sources() {
        if (jigTypes.empty()) {
            return DiagramSource.empty();
        }

        String structureText = jigTypes.list().stream()
                .map(categoryType -> {
                    StringJoiner categoryValues = new StringJoiner("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>");

                    List<StaticFieldDeclaration> list = categoryType.staticMember().staticFieldDeclarations().selfDefineOnly().list();
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 20) {
                            categoryValues.add("... more");
                            break;
                        }
                        String nameText = list.get(i).nameText();
                        categoryValues.add(nameText);
                    }
                    String categoryName = categoryType.nodeLabel("<br/>");

                    return Node.typeOf(categoryType.typeIdentifier())
                            .html(categoryName, "<table border=\"0\" cellspacing=\"0\"><tr><td>" + categoryName + "</td></tr>" + categoryValues + "</table>")
                            .url(categoryType.typeIdentifier(), JigDocument.EnumSummary)
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
