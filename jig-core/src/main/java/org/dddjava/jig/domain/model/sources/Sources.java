package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.BinarySources;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.mybatis.SqlSources;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 生ソース
 */
public class Sources {

    private final SourceBasePaths sourceBasePaths;
    private final JavaSources javaSources;
    private final BinarySources binarySources;

    public Sources(SourceBasePaths sourceBasePaths, JavaSources javaSources, BinarySources binarySources) {
        this.sourceBasePaths = sourceBasePaths;
        this.javaSources = javaSources;
        this.binarySources = binarySources;
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public SqlSources sqlSources() {
        URL[] classLocationUrls = sourceBasePaths.classSourceBasePaths().stream()
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                }).toArray(URL[]::new);
        List<String> mapperClassNames = binarySources.classNames(name -> name.endsWith("Mapper"));
        // クラスのURLとクラス名を別のリストで渡しているけれど、クラスごとにURL明確なのでMapで渡したほうがよさそう
        return new SqlSources(classLocationUrls, mapperClassNames);
    }

    public boolean nothingBinarySource() {
        return binarySources.nothing();
    }

    public boolean nothingTextSource() {
        return javaSources.nothing();
    }

    public ClassSources classSources() {
        return binarySources.classSources();
    }
}
