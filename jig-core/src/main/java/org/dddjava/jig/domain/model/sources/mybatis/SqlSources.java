package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.ClassSource;

import java.util.List;

/**
 * SQLの情報源
 */
public class SqlSources {
    private final SourceBasePaths sourceBasePaths;
    private final List<ClassSource> classSources;

    public SqlSources(SourceBasePaths sourceBasePaths, List<ClassSource> classSources) {
        this.sourceBasePaths = sourceBasePaths;
        this.classSources = classSources;
    }

    public static SqlSources from(Sources sources) {
        List<ClassSource> mapperClassSource = sources.classSources().filterClassName(name -> name.endsWith("Mapper"));
        return new SqlSources(sources.sourceBasePaths(), mapperClassSource);
    }

    public List<String> classNames() {
        return classSources.stream().map(classSource -> classSource.className()).toList();
    }

    public SourceBasePaths sourceBasePaths() {
        return sourceBasePaths;
    }
}
