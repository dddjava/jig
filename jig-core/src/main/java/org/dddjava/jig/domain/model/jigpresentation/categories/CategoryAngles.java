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

    TypeIdentifiers userTypeIdentifiers() {
        List<TypeIdentifier> userTypeIdentifiers = list().stream()
                .flatMap(categoryAngle -> categoryAngle.userTypeIdentifiers().list().stream())
                .distinct()
                .filter(this::notCategory)
                .collect(Collectors.toList());
        return new TypeIdentifiers(userTypeIdentifiers);
    }

    boolean notCategory(TypeIdentifier typeIdentifier) {
        return list.stream()
                .noneMatch(categoryAngle -> categoryAngle.categoryType.typeIdentifier.equals(typeIdentifier));
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

    private String typeNameOf(TypeIdentifier typeIdentifier, AliasFinder aliasFinder) {
        TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
        if (typeAlias.exists()) {
            return typeAlias.asText() + "\\n" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }

    private String typeNameOf(AliasFinder aliasFinder, TypeIdentifier typeIdentifier) {
        TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
        if (typeAlias.exists()) {
            return typeAlias.asText() + "<br/>" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }

    public DiagramSources toUsageDotText(AliasFinder aliasFinder, JigDocumentContext jigDocumentContext) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        TypeIdentifiers enumTypes = list.stream()
                .map(CategoryAngle::typeIdentifier)
                .collect(TypeIdentifiers.collector());

        String enumsText = enumTypes.list().stream()
                .map(enumType -> Node.of(enumType)
                        .label(typeNameOf(enumType, aliasFinder))
                        .asText())
                .collect(joining("\n"));

        RelationText relationText = new RelationText();
        for (CategoryAngle categoryAngle : list()) {
            for (TypeIdentifier userType : categoryAngle.userTypeIdentifiers().list()) {
                relationText.add(userType, categoryAngle.typeIdentifier());
            }
        }

        String userLabel = userTypeIdentifiers().list().stream()
                .map(typeIdentifier ->
                        Node.of(typeIdentifier)
                                .label(typeNameOf(typeIdentifier, aliasFinder))
                                .notEnum()
                                .asText())
                .collect(joining("\n"));

        String legendText = new Subgraph("legend")
                .label(jigDocumentContext.label("legend"))
                .add(new Node(jigDocumentContext.label("enum")).asText())
                .add(new Node(jigDocumentContext.label("not_enum")).notEnum().asText())
                .toString();

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph JIG {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(legendText)
                .add("{ rank=same;")
                .add(enumsText)
                .add("}")
                .add(relationText.asText())
                .add(userLabel)
                .toString());
    }
}
