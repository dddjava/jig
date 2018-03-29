package jig.infrastructure.plantuml;

import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.diagram.DiagramSource;
import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;

import static java.util.stream.Collectors.joining;

public class PlantumlDiagramConverter implements DiagramConverter {

    PackageIdentifierFormatter formatter;
    JapaneseNameRepository repository;

    public PlantumlDiagramConverter(PackageIdentifierFormatter formatter, JapaneseNameRepository repository) {
        this.formatter = formatter;
        this.repository = repository;
    }

    @Override
    public DiagramSource toDiagramSource(PackageDependencies packageDependencies) {
        String source = "@startuml\n" +
                "hide members\n" +
                "hide circle\n" +
                "set namespaceSeparator none\n" +
                "\n" +
                classes(packageDependencies) +
                "\n" +
                packageDependencies.list().stream()
                        .map(this::relationToString)
                        .collect(joining("\n")) +
                "\n" +
                "footer %date%\n" +
                "@enduml";
        return new DiagramSource(source);
    }

    private String classes(PackageDependencies packageDependencies) {
        return packageDependencies.allPackages().stream()
                .map(packageIdentifier -> "class " + packageIdentifier.format(formatter) + japaneseName(packageIdentifier))
                .collect(joining("\n"));
    }

    private String japaneseName(PackageIdentifier name) {
        if (!repository.exists(name)) {
            return "";
        }

        String summary = repository.get(name).summarySentence();
        // 改行があると出力エラーになるので、改行の手前までにする。
        String firstLine = summary.replaceAll("(\r\n|[\n\r\u2028\u2029\u0085]).+", "");
        return "<<" + firstLine + ">>";
    }

    String relationToString(PackageDependency packageDependency) {
        return String.format("\"%s\" ..> \"%s\"",
                packageDependency.from().format(formatter),
                packageDependency.to().format(formatter)
        );
    }
}
