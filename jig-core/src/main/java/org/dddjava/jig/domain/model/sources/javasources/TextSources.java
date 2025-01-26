package org.dddjava.jig.domain.model.sources.javasources;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * テキストソース一覧
 */
public class TextSources {

    Map<TextSourceType, List<TextSource>> map;

    public TextSources(List<TextSource> list) {
        this.map = list.stream().collect(Collectors.groupingBy(TextSource::textSourceType));
    }

    public boolean nothing() {
        return map.isEmpty();
    }

    public ReadableTextSources packageInfoSources() {
        return toReadableSources(TextSourceType.JAVA_PACKAGE_INFO);
    }

    public ReadableTextSources javaSources() {
        return toReadableSources(TextSourceType.JAVA);
    }

    private ReadableTextSources toReadableSources(TextSourceType javaPackageInfo) {
        List<TextSource> textSources = map.getOrDefault(javaPackageInfo, Collections.emptyList());
        return new ReadableTextSources(textSources);
    }
}
