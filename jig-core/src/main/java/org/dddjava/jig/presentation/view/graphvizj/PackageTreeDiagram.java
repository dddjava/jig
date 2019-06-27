package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.AliasFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.PackageAlias;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * パッケージツリー
 */
public class PackageTreeDiagram implements DotTextEditor<AllPackageIdentifiers> {

    AliasFinder aliasFinder;

    public PackageTreeDiagram(PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
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
                            .label(packageLabelOf(packageIdentifier) + subPackages)
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

    private String packageLabelOf(PackageIdentifier packageIdentifier) {
        PackageAlias alias = aliasFinder.find(packageIdentifier);
        if (alias.exists()) {
            return alias.asText() + "\\n" + packageIdentifier.simpleName();
        }
        return packageIdentifier.simpleName();
    }
}
