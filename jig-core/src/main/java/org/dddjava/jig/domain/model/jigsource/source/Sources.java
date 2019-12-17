package org.dddjava.jig.domain.model.jigsource.source;

import org.dddjava.jig.domain.model.jigsource.source.binary.BinarySources;
import org.dddjava.jig.domain.model.jigsource.source.binary.ClassSources;
import org.dddjava.jig.domain.model.jigsource.source.code.AliasSource;
import org.dddjava.jig.domain.model.jigsource.source.code.CodeSources;
import org.dddjava.jig.domain.model.jigsource.source.code.sqlcode.SqlSources;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 生ソース
 */
public class Sources {

    CodeSources codeSources;
    BinarySources binarySources;

    public Sources(CodeSources codeSources, BinarySources binarySources) {
        this.codeSources = codeSources;
        this.binarySources = binarySources;
    }

    public AliasSource aliasSource() {
        return codeSources.aliasSource();
    }

    public SqlSources sqlSources() {
        URL[] urls = binarySources.list().stream()
                .map(binarySource -> {
                    try {
                        return binarySource.sourceLocation().uri().toURL();
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toArray(URL[]::new);
        List<String> classNames = binarySources.list().stream()
                .flatMap(binarySource -> binarySource.classSources().list().stream())
                .map(classSource -> classSource.className())
                .filter(name -> name.endsWith("Mapper"))
                .collect(toList());
        return new SqlSources(urls, classNames);
    }

    public boolean nothingBinarySource() {
        return binarySources.nothing();
    }

    public boolean nothingTextSource() {
        return codeSources.nothing();
    }

    public ClassSources classSources() {
        return binarySources.toBinarySource().classSources();
    }
}
