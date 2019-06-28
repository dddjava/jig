package org.dddjava.jig.domain.model.fact.source.code.sqlcode;

import java.net.URL;
import java.util.List;

/**
 * SQLの情報源
 */
public class SqlSources {
    private final URL[] urls;
    private final List<String> classNames;

    public SqlSources(URL[] urls, List<String> classNames) {
        this.urls = urls;
        this.classNames = classNames;
    }

    public URL[] urls() {
        return urls;
    }

    public List<String> classNames() {
        return classNames;
    }
}
