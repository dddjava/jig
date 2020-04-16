package org.dddjava.jig.domain.model.jigpresentation.diagram;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloader.MethodFactory;
import org.dddjava.jig.domain.model.jigloader.RelationsFactory;
import org.dddjava.jig.domain.model.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * 区分使用図
 */
public class CategoryUsageDiagram {

    ServiceMethods serviceMethods;
    CategoryTypes categoryTypes;
    ClassRelations classRelations;

    public CategoryUsageDiagram(CategoryTypes categoryTypes, AnalyzedImplementation analyzedImplementation, Architecture architecture) {
        this.categoryTypes = categoryTypes;

        List<TypeByteCode> collect = analyzedImplementation.typeByteCodes().list()
                .stream()
                .filter(typeByteCode -> architecture.isBusinessRule(typeByteCode))
                .collect(Collectors.toList());
        this.classRelations = RelationsFactory.createClassRelations(new TypeByteCodes(collect));

        this.serviceMethods = MethodFactory.createServiceMethods(analyzedImplementation.typeByteCodes(), architecture);
    }

    ClassRelations relations() {
        HashSet<ClassRelation> set = new HashSet<>();

        TypeIdentifiers toTypeIdentifiers = categoryTypes.typeIdentifiers();
        int size = set.size();
        while (true) {
            ClassRelations temp = classRelations.filterRelationsTo(toTypeIdentifiers);
            set.addAll(temp.list());

            if (size == set.size()) break;
            size = set.size();
            toTypeIdentifiers = temp.fromTypeIdentifiers();
        }
        return new ClassRelations(new ArrayList<>(set));
    }

    public DiagramSources diagramSource(AliasFinder aliasFinder, JigDocumentContext jigDocumentContext) {
        if (categoryTypes.isEmpty()) {
            return DiagramSource.empty();
        }

        String enumsText = categoryTypes.list().stream()
                .map(categoryType -> categoryType.typeIdentifier())
                .map(typeIdentifier -> Node.of(typeIdentifier)
                        .normalColor()
                        .label(typeNameOf(typeIdentifier, aliasFinder))
                        .asText())
                .collect(joining("\n"));

        ClassRelations relations = relations();
        TypeIdentifiers typeIdentifiers = relations.collectTypeIdentifiers();
        StringJoiner nodeTexts = new StringJoiner("\n");
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            if (categoryTypes.contains(typeIdentifier)) {
                continue;
            }

            Node node = Node.of(typeIdentifier)
                    .label(aliasFinder.find(typeIdentifier).asTextOrDefault(typeIdentifier.asSimpleText()));
            nodeTexts.add(node.asText());
        }

        StringJoiner useCaseText = new StringJoiner("\n");
        RelationText serviceRelationText = new RelationText();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            boolean relateService = false;
            TypeIdentifiers serviceUsingTypes = serviceMethod.usingTypes();
            for (TypeIdentifier usingTypeIdentifier : serviceUsingTypes.list()) {
                if (typeIdentifiers.contains(usingTypeIdentifier)) {
                    serviceRelationText.add(serviceMethod.methodDeclaration(), usingTypeIdentifier);
                    relateService = true;
                }
            }
            if (!relateService) {
                // enumから関連していないのは出力しない
                continue;
            }

            Node node = Node.of(serviceMethod.methodDeclaration())
                    .label(useCaseLabel(serviceMethod, aliasFinder))
                    .normalColor()
                    .useCase();
            useCaseText.add(node.asText());
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.CategoryUsageDiagram);
        return DiagramSource.createDiagramSource(documentName, new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                //.add("node [shape=box,style=filled,fillcolor=lightgoldenrod];")
                .add("node [shape=box,style=filled,fillcolor=white];")
                .add("{")
                .add("rank=sink;")
                .add(enumsText)
                .add("}")
                .add("{")
                .add("rank=source;")
                .add(useCaseText.toString())
                .add("}")
                .add(nodeTexts.toString())
                .add(RelationText.fromClassRelation(relations).asText())
                .add(serviceRelationText.asText())
                .toString());
    }

    String useCaseLabel(ServiceMethod serviceMethod, AliasFinder aliasFinder) {
        MethodAlias methodAlias = aliasFinder.find(serviceMethod.methodDeclaration().identifier());
        return methodAlias.asTextOrDefault(serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName());
    }

    private String typeNameOf(TypeIdentifier typeIdentifier, AliasFinder aliasFinder) {
        TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
        if (typeAlias.exists()) {
            return typeAlias.asText() + "\\n" + typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText();
    }
}
