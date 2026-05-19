package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 複数の設定ソースを「優先度高 → 低」の順にマージし、最終的な {@link JigSettings} を構築する。
 *
 * マージ規則は first non-empty wins: 各フィールドごとに、リスト先頭から走査し最初に値が
 * 存在するソースを採用する。どの層も指定していなければ {@link JigSettings#defaults()} の値を用いる。
 *
 * 標準的な優先順位（{@link #loadStandard}）は以下:
 * <ol>
 *   <li>実行形式固有の設定（CLI 引数 / Gradle extension）</li>
 *   <li>{@code {user.dir}/jig.properties}</li>
 *   <li>{@code {user.home}/.jig/jig.properties}</li>
 *   <li>デフォルト値</li>
 * </ol>
 */
public class JigSettingsLoader {

    public static JigSettings load(List<PartialJigSettings> highToLow) {
        JigSettings defaults = JigSettings.defaults();
        List<JigDocument> docs = highToLow.stream()
                .map(PartialJigSettings::documentTypes)
                .filter(list -> !list.isEmpty())
                .findFirst()
                .orElse(defaults.documentTypes());
        return new JigSettings(
                pick(highToLow, PartialJigSettings::outputDirectory).orElse(defaults.outputDirectory()),
                pick(highToLow, PartialJigSettings::domainPattern),
                docs,
                pick(highToLow, PartialJigSettings::locale).orElse(defaults.locale())
        );
    }

    public static JigSettings loadStandard(PartialJigSettings explicit) {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path userHomeJig = Paths.get(System.getProperty("user.home")).resolve(".jig");
        return load(List.of(
                explicit,
                new PropertiesFileSource(userDir).read(),
                new PropertiesFileSource(userHomeJig).read()
        ));
    }

    private static <T> Optional<T> pick(List<PartialJigSettings> sources,
                                        Function<PartialJigSettings, Optional<T>> selector) {
        return sources.stream()
                .map(selector)
                .flatMap(Optional::stream)
                .findFirst();
    }
}
