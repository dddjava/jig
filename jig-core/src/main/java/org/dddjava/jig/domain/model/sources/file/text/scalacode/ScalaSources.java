package org.dddjava.jig.domain.model.sources.file.text.scalacode;

import java.util.Collections;
import java.util.List;

/**
 * .scalaソース一覧
 */
public class ScalaSources {

    List<ScalaSource> list;

    public ScalaSources(List<ScalaSource> list) {
        this.list = list;
    }

    public ScalaSources() {
        this(Collections.emptyList());
    }

    public List<ScalaSource> list() {
        return list;
    }
}
