package org.dddjava.jig.domain.model.sources.jigreader;

public class AdditionalTextSourceReader {

    KotlinTextSourceReader kotlinTextSourceReader;
    ScalaSourceAliasReader scalaSourceAliasReader;

    public AdditionalTextSourceReader(Object... objects) {
        for (Object object : objects) {
            if (object instanceof KotlinTextSourceReader) {
                this.kotlinTextSourceReader = (KotlinTextSourceReader) object;
            }
            if (object instanceof ScalaSourceAliasReader) {
                this.scalaSourceAliasReader = (ScalaSourceAliasReader) object;
            }
        }
    }

    public KotlinTextSourceReader kotlinTextSourceReader() {
        if (kotlinTextSourceReader == null) return arg -> ClassAndMethodComments.empty();
        return kotlinTextSourceReader;
    }

    public ScalaSourceAliasReader scalaSourceAliasReader() {
        if (scalaSourceAliasReader == null) return arg -> ClassAndMethodComments.empty();
        return scalaSourceAliasReader;
    }
}
