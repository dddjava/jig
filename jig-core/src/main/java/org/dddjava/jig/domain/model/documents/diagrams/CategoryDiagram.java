package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;

import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

/**
 * 区分図
 */
public class CategoryDiagram implements DiagramSourceWriter {

    private final CategoryTypes categoryTypes;

    CategoryDiagram(CategoryTypes categoryTypes) {
        this.categoryTypes = categoryTypes;
    }

    public static CategoryDiagram create(CategoryTypes categoryTypes) {
        return new CategoryDiagram(categoryTypes);
    }

    public List<CategoryType> list() {
        return categoryTypes.list();
    }

    public DiagramSources sources() {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        String structureText = categoryTypes.list().stream()
                .map(categoryType -> {
                    StringJoiner categoryValues = new StringJoiner("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>");

                    List<StaticFieldDeclaration> list = categoryType.values().list();
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
