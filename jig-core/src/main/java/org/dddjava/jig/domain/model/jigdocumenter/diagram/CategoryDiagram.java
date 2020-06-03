package org.dddjava.jig.domain.model.jigdocumenter.diagram;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSource;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSources;
import org.dddjava.jig.domain.model.jigdocumenter.JigDocumentContext;
import org.dddjava.jig.domain.model.jigdocumenter.Subgraph;
import org.dddjava.jig.domain.model.jigdocumenter.categories.CategoryAngle;
import org.dddjava.jig.domain.model.jigdocumenter.categories.PackageStructure;
import org.dddjava.jig.domain.model.jigmodel.Node;
import org.dddjava.jig.domain.model.jigmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryType;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * 区分の切り口一覧
 */
// TODO Diagram以外にも使われている。。。
public class CategoryDiagram {

    List<CategoryAngle> list;

    public CategoryDiagram(List<CategoryAngle> list) {
        this.list = list;
    }

    public static CategoryDiagram categoryDiagram(CategoryTypes categoryTypes, ClassRelations classRelations, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        List<CategoryAngle> list = new ArrayList<>();
        for (CategoryType categoryType : categoryTypes.list()) {
            list.add(new CategoryAngle(categoryType, classRelations, fieldDeclarations, staticFieldDeclarations));
        }
        return new CategoryDiagram(list);
    }

    public List<CategoryAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(categoryAngle -> categoryAngle.typeIdentifier()))
                .collect(toList());
    }

    public DiagramSources valuesDotText(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        List<TypeIdentifier> categoryTypeIdentifiers = list.stream()
                .map(categoryAngle -> categoryAngle.typeIdentifier())
                .collect(toList());

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
                documentName, new StringJoiner("\n", "graph \"" + documentName.label() + "\" {", "}")
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
