package jig.domain.model.dependency;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisCriteria {

    public final SearchPaths paths;

    public final AnalysisClassesPattern analysisClassesPattern;

    public final DependenciesPattern dependenciesPattern;

    public AnalysisCriteria(SearchPaths paths, AnalysisClassesPattern analysisClassesPattern, DependenciesPattern dependenciesPattern) {
        this.paths = paths;
        this.analysisClassesPattern = analysisClassesPattern;
        this.dependenciesPattern = dependenciesPattern;
    }

    public List<String> addAllPath(List<String> args) {
        List<String> list = new ArrayList<>(args);
        List<String> paths = this.paths.list.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.toList());
        list.addAll(paths);
        return list;
    }
}
