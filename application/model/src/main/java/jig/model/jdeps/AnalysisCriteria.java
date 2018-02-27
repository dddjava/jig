package jig.model.jdeps;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisCriteria {

    final SearchPaths paths;

    final AnalysisClassesPattern analysisClassesPattern;

    final DependenciesPattern dependenciesPattern;

    final AnalysisTarget target;

    public AnalysisCriteria(SearchPaths paths, AnalysisClassesPattern analysisClassesPattern, DependenciesPattern dependenciesPattern, AnalysisTarget target) {
        this.paths = paths;
        this.analysisClassesPattern = analysisClassesPattern;
        this.dependenciesPattern = dependenciesPattern;
        this.target = target;
    }

    public List<String> toJdepsArgs() {
        List<String> list = new ArrayList<>(Arrays.asList(
                "-include",
                analysisClassesPattern.value(),
                "-e",
                dependenciesPattern.value()
        ));
        if (target == AnalysisTarget.CLASS) {
            list.add("-verbose:class");
            list.add("-filter:none");
        }
        List<String> paths = this.paths.list.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.toList());
        list.addAll(paths);
        return list;
    }
}
