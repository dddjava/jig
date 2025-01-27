package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassSource;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;

import java.util.List;

/**
 * 生ソース
 */
public class Sources {

    private final SourceBasePaths sourceBasePaths;
    private final JavaSources javaSources;
    private final ClassSources classSources;

    public Sources(SourceBasePaths sourceBasePaths, JavaSources javaSources, ClassSources classSources) {
        this.sourceBasePaths = sourceBasePaths;
        this.javaSources = javaSources;
        this.classSources = classSources;
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public SqlSources sqlSources() {
        List<ClassSource> mapperClassSource = classSources.filterClassName(name -> name.endsWith("Mapper"));
        return new SqlSources(sourceBasePaths, mapperClassSource);
    }

    public boolean emptyClassSources() {
        return classSources.nothing();
    }

    public boolean emptyJavaSources() {
        return javaSources.nothing();
    }

    public ClassSources classSources() {
        return classSources;
    }
}
