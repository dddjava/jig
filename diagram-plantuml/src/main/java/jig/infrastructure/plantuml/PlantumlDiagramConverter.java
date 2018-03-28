package jig.infrastructure.plantuml;

import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.diagram.DiagramSource;
import jig.domain.model.identifier.NameFormatter;
import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;

import static java.util.stream.Collectors.joining;

public class PlantumlDiagramConverter implements DiagramConverter {

    NameFormatter nameFormatter;
    JapaneseNameRepository repository;

    public PlantumlDiagramConverter(NameFormatter nameFormatter,
                                    JapaneseNameRepository repository) {
        this.nameFormatter = nameFormatter;
        this.repository = repository;
    }

    @Override
    public DiagramSource toDiagramSource(PackageDependencies packageDependencies) {
        String source = "@startuml\n" +
                "hide members\n" +
                "hide circle\n" +
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
                .map(name -> "class " + nameFormatter.format(name) + japaneseName(name))
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
                nameFormatter.format(packageDependency.from()),
                nameFormatter.format(packageDependency.to())
        );
    }
}
