package org.dddjava.jig.domain.model.architectures;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigdocument.DotText;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.Node;
import org.dddjava.jig.domain.model.jigdocument.RelationText;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.jigloaded.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelations;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArchitectureAngle {

    AnalyzedImplementation analyzedImplementation;

    public ArchitectureAngle(AnalyzedImplementation analyzedImplementation) {
        this.analyzedImplementation = analyzedImplementation;
    }

    public DotText dotText(JigDocumentContext jigDocumentContext) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ClassRelations classRelations = new ClassRelations(typeByteCodes);
        PackageRelations packageRelations = PackageRelations.fromClassRelations(classRelations);

        if (!packageRelations.available()) {
            return DotText.empty();
        }

        Pattern pattern = Pattern.compile("(.*\\.)(application|domain|infrastructure|presentation)\\..*");
        List<PackageRelation> list = packageRelations.list()
                .stream()
                .filter(packageRelation -> {
                    String to = packageRelation.to().asText();
                    return !to.startsWith("[L") && !to.startsWith("java");
                })
                .filter(packageRelation ->
                        pattern.matcher(packageRelation.from().asText()).matches()
                                && pattern.matcher(packageRelation.to().asText()).matches())
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return DotText.empty();
        }

        String packageName = list.stream()
                .findFirst()
                .map(packageRelation -> packageRelation.from())
                .map(PackageIdentifier::asText)
                .orElseThrow(IllegalStateException::new);

        Matcher matcher = pattern.matcher(packageName);
        matcher.matches();
        String prefixPackage = matcher.group(1);

        int depth = prefixPackage.split("\\.").length;
        if (depth == 0) {
            return DotText.empty();
        }
        depth++;

        PackageRelations architectureRelation = new PackageRelations(list).applyDepth(new PackageDepth(depth));

        StringJoiner graph = new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.ArchitectureDiagram) + "\";")
                .add(Node.DEFAULT);

        RelationText relationText = new RelationText();
        for (PackageRelation packageRelation : architectureRelation.list()) {
            relationText.add(packageRelation.from(), packageRelation.to());
        }
        graph.add(relationText.asText());

        return new DotText(graph.toString());
    }
}
