package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.sources.classsources.ClassSource;

import java.net.URL;
import java.util.List;

/**
 * SQLの情報源
 */
public class SqlSources {
    private final URL[] urls;
    private final List<ClassSource> classSources;

    public SqlSources(URL[] urls, List<ClassSource> classSources) {
        this.urls = urls;
        this.classSources = classSources;
    }

    public URL[] urls() {
        return urls;
    }

    public List<String> classNames() {
        return classSources.stream().map(classSource -> classSource.className()).toList();
    }
}
