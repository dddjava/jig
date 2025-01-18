package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        Map<TypeIdentifier, CategoryType> map = categoryTypes.list().stream()
                .collect(toMap(CategoryType::typeIdentifier, Function.identity()));

        PackageStructure packageStructure = PackageStructure.from(new ArrayList<>(map.keySet()));

        String structureText = packageStructure.toDotText(
                typeIdentifier -> {
                    CategoryType categoryType = map.get(typeIdentifier);

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

                    Node node = Node.typeOf(typeIdentifier)
                            .html(categoryName, "<table border=\"0\" cellspacing=\"0\"><tr><td>" + categoryName + "</td></tr>" + categoryValues + "</table>")
                            .url(typeIdentifier, JigDocument.EnumSummary);
                    return node.as(categoryType.hasBehaviour() ? NodeRole.主役 : NodeRole.準主役);
                }
        );

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
