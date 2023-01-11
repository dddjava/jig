package org.dddjava.jig.domain.model.sources.file;

import org.dddjava.jig.domain.model.sources.file.binary.BinarySources;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.file.text.TextSources;
import org.dddjava.jig.domain.model.sources.file.text.sqlcode.SqlSources;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 生ソース
 */
public class Sources {

    TextSources textSources;
    BinarySources binarySources;

    public Sources(TextSources textSources, BinarySources binarySources) {
        this.textSources = textSources;
        this.binarySources = binarySources;
    }

    public TextSources textSources() {
        return textSources;
    }

    public SqlSources sqlSources() {
        URL[] classLocationUrls = binarySources.list().stream()
                .map(binarySource -> {
                    try {
                        return binarySource.sourceLocation().uri().toURL();
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toArray(URL[]::new);
        List<String> mapperClassNames = binarySources.classNames(name -> name.endsWith("Mapper"));
        // クラスのURLとクラス名を別のリストで渡しているけれど、クラスごとにURL明確なのでMapで渡したほうがよさそう
        return new SqlSources(classLocationUrls, mapperClassNames);
    }

    public boolean nothingBinarySource() {
        return binarySources.nothing();
    }

    public boolean nothingTextSource() {
        return textSources.nothing();
    }

    public ClassSources classSources() {
        return binarySources.toBinarySource().classSources();
    }
}
