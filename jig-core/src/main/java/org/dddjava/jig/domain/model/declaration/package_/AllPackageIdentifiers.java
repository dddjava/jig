package org.dddjava.jig.domain.model.declaration.package_;

import org.dddjava.jig.domain.model.diagram.DotText;
import org.dddjava.jig.domain.model.diagram.Node;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.alias.PackageAlias;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class AllPackageIdentifiers {

    List<PackageIdentifier> list;

    AllPackageIdentifiers(List<PackageIdentifier> list) {
        this.list = new ArrayList<>();
        for (PackageIdentifier packageIdentifier : list) {
            if (this.list.contains(packageIdentifier)) {
                continue;
            }
            this.list.add(packageIdentifier);

            PackageIdentifier tmp = packageIdentifier.parent();
            while (tmp.hasName()) {
                if (this.list.contains(tmp)) break;
                this.list.add(tmp);
                tmp = tmp.parent();
            }
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<PackageIdentifier> list() {
        return list;
    }

    public DotText getDotText(AliasFinder aliasFinder) {
        Map<PackageIdentifier, List<PackageIdentifier>> map = new HashMap<>();
        for (PackageIdentifier packageIdentifier : list()) {
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
                            .label(packageLabelOf(packageIdentifier, aliasFinder) + subPackages)
                            .asText();
                })
                .collect(joining("\n"));

        String relations = list().stream()
                .map(packageIdentifier -> {
                    PackageIdentifier parent = packageIdentifier.parent();
                    return String.format("\"%s\":\"%s\" -> \"%s\";", parent.asText(), packageIdentifier.asText(), packageIdentifier.asText());
                })
                .collect(Collectors.joining("\n"));
        return new DotText(new StringJoiner("\n", "digraph {", "}")
                .add("rankdir=LR;")
                .add("node [shape=Mrecord;style=filled;fillcolor=\"#FAD689\";color=\"#82663A\"];")
                .add("edge [color=\"#82663A\"];")
                .add("# records")
                .add(records)
                .add("# relations")
                .add(relations)
                .toString());
    }

    private String packageLabelOf(PackageIdentifier packageIdentifier, AliasFinder aliasFinder) {
        PackageAlias alias = aliasFinder.find(packageIdentifier);
        if (alias.exists()) {
            return alias.asText() + "\\n" + packageIdentifier.simpleName();
        }
        return packageIdentifier.simpleName();
    }
}
