package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * 複数の設定ソースを「優先度高 → 低」の順にマージし、最終的な {@link JigSettings} を構築する。
 *
 * マージ規則は first non-empty wins: 各フィールドごとに、リスト先頭から走査し最初に値が
 * 存在するソースを採用する。jig-core のフィールド既定値（{@link JigSettings#defaults()}）は
 * {@link #load} 内で常に最低優先度層として自動的に追加される。
 *
 * 出力先ディレクトリ（{@link JigSettings#outputDirectory}）はどの層も指定していない場合に
 * 例外を投げる。jig-core が任意のパスをデフォルトとして決めるべきではないため、CLI / Gradle
 * plugin / テストなど呼び出し側が必ず指定する責任を負う。
 *
 * 標準的な優先順位（{@link #loadStandard}）は以下:
 * <ol>
 *   <li>実行形式固有の設定（CLI 引数 {@code --jig.*} / Gradle extension）</li>
 *   <li>システムプロパティ {@code -Djig.*}（{@link SystemPropertySource}）</li>
 *   <li>環境変数 {@code JIG_*}（{@link EnvironmentVariableSource}）</li>
 *   <li>{@code {user.dir}/jig.properties}</li>
 *   <li>{@code {user.home}/.jig/jig.properties}</li>
 *   <li>{@code fallback}（呼び出し側ランタイム固有のデフォルト）</li>
 *   <li>{@link JigSettings#defaults()}（jig-core のフィールド既定値、{@link #load} が自動追加）</li>
 * </ol>
 */
public class JigSettingsLoader {

    public static JigSettings load(List<PartialJigSettings> highToLow) {
        List<PartialJigSettings> layers = new ArrayList<>(highToLow);
        layers.add(JigSettings.defaults());

        Path outputDirectory = pick(layers, PartialJigSettings::outputDirectory)
                .orElseThrow(() -> new IllegalStateException(
                        "出力先ディレクトリが指定されていません。jig.output.directory もしくは呼び出し側で必ず指定してください。"));
        List<JigDocument> docs = layers.stream()
                .map(PartialJigSettings::jigDocuments)
                .filter(list -> !list.isEmpty())
                .findFirst()
                .orElseThrow(); // JigSettings.defaults() が必ず documentTypes を持つので到達しない
        Locale locale = pick(layers, PartialJigSettings::locale)
                .orElseThrow(); // JigSettings.defaults() が必ず locale を持つので到達しない
        return new JigSettings(
                outputDirectory,
                pick(layers, PartialJigSettings::domainPattern),
                docs,
                locale
        );
    }

    public static JigSettings loadStandard(PartialJigSettings explicit) {
        return loadStandard(explicit, PartialJigSettings.EMPTY);
    }

    /**
     * 標準優先順位での読み込みに、{@code fallback}（呼び出し側ランタイム固有のデフォルト）層を追加する。
     *
     * 例: CLI は {@code ./build/jig} を出力先として fallback で供給する。
     */
    public static JigSettings loadStandard(PartialJigSettings explicit, PartialJigSettings fallback) {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path userHomeJig = Paths.get(System.getProperty("user.home")).resolve(".jig");
        return load(List.of(
                explicit,
                SystemPropertySource.fromSystem().read(),
                EnvironmentVariableSource.fromSystem().read(),
                new PropertiesFileSource(userDir).read(),
                new PropertiesFileSource(userHomeJig).read(),
                fallback
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
