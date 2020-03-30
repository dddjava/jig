package org.dddjava.jig.domain.model.jigpresentation.categories;

import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * 区分の切り口一覧
 */
public class CategoryAngles {

    List<CategoryAngle> list;

    public CategoryAngles(CategoryTypes categoryTypes, AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        this.list = new ArrayList<>();
        for (CategoryType categoryType : categoryTypes.list()) {
            list.add(new CategoryAngle(categoryType, new ClassRelations(typeByteCodes), typeByteCodes.instanceFields(), typeByteCodes.staticFields()));
        }
    }

    public List<CategoryAngle> list() {
        return list;
    }

    public DiagramSources valuesDotText(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        List<TypeIdentifier> categoryTypeIdentifiers = list.stream()
                .map(categoryAngle -> categoryAngle.typeIdentifier())
                .collect(Collectors.toList());

        PackageStructure packageStructure = PackageStructure.from(categoryTypeIdentifiers);

        String structureText = packageStructure.toDotText(
                packageIdentifier -> new Subgraph(packageIdentifier.asText())
                        .label(packageIdentifier.simpleName()),
                typeIdentifier -> Node.of(typeIdentifier)
        );

        StringJoiner categoryText = new StringJoiner("\n");
        for (CategoryAngle categoryAngle : list) {
            String values = categoryAngle.constantsDeclarations().list().stream()
                    .map(StaticFieldDeclaration::nameText)
                    .collect(joining("</td></tr><tr><td border=\"1\">", "<tr><td border=\"1\">", "</td></tr>"));
            TypeIdentifier typeIdentifier = categoryAngle.typeIdentifier();
            String nodeText = new Node(typeIdentifier.fullQualifiedName())
                    .html("<table border=\"0\" cellspacing=\"0\"><tr><td>" + typeNameOf(aliasFinder, typeIdentifier) + "</td></tr>" + values + "</table>")
                    .asText();

            categoryText.add(nodeText);
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryDiagram);
        return DiagramSource.createDiagramSource(
                documentName, new StringJoiner("\n", "graph {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("layout=fdp;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(structureText)
                .add(categoryText.toString())
                .toString());
    }

    private String typeNameOf(AliasFinder aliasFinder, TypeIdentifier typeIdentifier) {
        TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
        if (typeAlias.exists()) {
            return typeAlias.asText() + "<br/>" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
