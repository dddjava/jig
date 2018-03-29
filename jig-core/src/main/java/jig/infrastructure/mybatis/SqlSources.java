package jig.infrastructure.mybatis;

import java.net.URL;
import java.util.List;

public class SqlSources {
    private final URL[] urls;
    private final List<String> classNames;

    public SqlSources(URL[] urls, List<String> classNames) {
        this.urls = urls;
        this.classNames = classNames;
    }

    public URL[] getUrls() {
        return urls;
    }

    public List<String> getClassNames() {
        return classNames;
    }
}
