package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.JapaneseNameFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageJapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifierFormatter;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * パッケージツリー
 */
public class PackageTreeDiagram implements DotTextEditor<AllPackageIdentifiers> {

    JapaneseNameFinder japaneseNameFinder;

    public PackageTreeDiagram(PackageIdentifierFormatter formatter, JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public DotTexts edit(AllPackageIdentifiers model) {
        if (model.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
        }

        Map<PackageIdentifier, List<PackageIdentifier>> map = new HashMap<>();
        for (PackageIdentifier packageIdentifier : model.list()) {
            map.putIfAbsent(packageIdentifier, new ArrayList<>());
            PackageIdentifier parent = packageIdentifier.parent();
            map.putIfAbsent(parent, new ArrayList<>());
            map.get(parent).add(packageIdentifier);
        }

        String records = map.entrySet().stream()
                .map(entry -> {
                    String subPackages = "";
                    if (!entry.getValue().isEmpty()) {
                        subPackages = "|{ |"
                                + entry.getValue().stream()
                                .map(packageIdentifier -> String.format("<%s>%s", packageIdentifier.asText(), packageIdentifier.simpleName()))
                                .collect(joining("|", "{", "}"))
                                + "}";
                    }
                    PackageIdentifier packageIdentifier = entry.getKey();
                    return new Node(packageIdentifier.asText())
                            .label(appendJapaneseName(packageIdentifier) + subPackages)
                            .asText();
                })
                .collect(joining("\n"));

        String relations = model.list().stream()
                .map(packageIdentifier -> {
                    PackageIdentifier parent = packageIdentifier.parent();
                    return String.format("\"%s\":\"%s\" -> \"%s\";", parent.asText(), packageIdentifier.asText(), packageIdentifier.asText());
                })
                .collect(Collectors.joining("\n"));

        return new DotTexts(new StringJoiner("\n", "digraph {", "}")
                .add("rankdir=LR;")
                .add("node [shape=Mrecord;style=filled;fillcolor=\"#FAD689\";color=\"#82663A\"];")
                .add("edge [color=\"#82663A\"];")
                .add("# records")
                .add(records)
                .add("# relations")
                .add(relations)
                .toString());
    }

    private String appendJapaneseName(PackageIdentifier packageIdentifier) {
        PackageJapaneseName japaneseName = japaneseNameFinder.find(packageIdentifier);
        if (japaneseName.exists()) {
            return japaneseName.japaneseName().summarySentence() + "\\n" + packageIdentifier.simpleName();
        }
        return packageIdentifier.simpleName();
    }
}
