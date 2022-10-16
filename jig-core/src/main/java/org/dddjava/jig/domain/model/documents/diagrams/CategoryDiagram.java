package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryAngle;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * 区分図
 */
public class CategoryDiagram implements DiagramSourceWriter {

    List<CategoryAngle> list;

    CategoryDiagram(List<CategoryAngle> list) {
        this.list = list;
    }

    public static CategoryDiagram create(CategoryTypes categoryTypes, ClassRelations classRelations) {
        List<CategoryAngle> list = new ArrayList<>();
        for (CategoryType categoryType : categoryTypes.list()) {
            list.add(new CategoryAngle(categoryType, classRelations));
        }
        return new CategoryDiagram(list);
    }

    public List<CategoryAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(categoryAngle -> categoryAngle.typeIdentifier()))
                .collect(toList());
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        Map<TypeIdentifier, CategoryAngle> map = list.stream()
                .collect(toMap(CategoryAngle::typeIdentifier, Function.identity()));

        PackageStructure packageStructure = PackageStructure.from(new ArrayList<>(map.keySet()));

        String structureText = packageStructure.toDotText(
                typeIdentifier -> {
                    CategoryAngle categoryAngle = map.get(typeIdentifier);

                    StringJoiner categoryValues = new StringJoiner("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>");

                    List<StaticFieldDeclaration> list = categoryAngle.categoryType.values().list();
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 20) {
                            categoryValues.add("... more");
                            break;
                        }
                        String nameText = list.get(i).nameText();
                        categoryValues.add(nameText);
                    }
                    String categoryName = categoryAngle.nodeLabel("<br/>");

                    Node node = Node.typeOf(typeIdentifier).html("<table border=\"0\" cellspacing=\"0\"><tr><td>" + categoryName + "</td></tr>" + categoryValues + "</table>");
                    return node.as(categoryAngle.hasBehaviour() ? NodeRole.主役 : NodeRole.準主役);
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
