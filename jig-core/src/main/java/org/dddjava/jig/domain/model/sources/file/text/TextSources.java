package org.dddjava.jig.domain.model.sources.file.text;

import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSource;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSourceFile;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

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

    public ReadableTextSources kotlinSources() {
        return toReadableSources(TextSourceType.KOTLIN);
    }

    public ScalaSources scalaSources() {
        ReadableTextSources readableTextSources = toReadableSources(TextSourceType.SCALA);
        List<ScalaSource> list = readableTextSources.list().stream()
                .map(readableTextSource -> new ScalaSource(
                        new ScalaSourceFile(readableTextSource.path()), readableTextSource.bytes()))
                .collect(Collectors.toList());
        return new ScalaSources(list);
    }

    private ReadableTextSources toReadableSources(TextSourceType javaPackageInfo) {
        List<TextSource> textSources = map.getOrDefault(javaPackageInfo, Collections.emptyList());
        return new ReadableTextSources(textSources);
    }
}
