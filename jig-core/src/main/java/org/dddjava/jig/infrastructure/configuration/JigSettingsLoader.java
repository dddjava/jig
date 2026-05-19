package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * 複数の設定ソースを「優先度高 → 低」の順にマージし、最終的な {@link JigSettings} を構築する。
 *
 * マージ規則は first non-empty wins: 各フィールドごとに、リスト先頭から走査し最初に値が
 * 存在するソースを採用する。
 *
 * 出力先ディレクトリ（{@link JigSettings#outputDirectory}）はどの層も指定していない場合に
 * 例外を投げる。jig-core が任意のパスをデフォルトとして決めるべきではないため、CLI / Gradle
 * plugin / テストなど呼び出し側が必ず指定する責任を負う。
 *
 * 標準的な優先順位（{@link #loadStandard}）は以下:
 * <ol>
 *   <li>実行形式固有の設定（CLI 引数 / Gradle extension）</li>
 *   <li>{@code {user.dir}/jig.properties}</li>
 *   <li>{@code {user.home}/.jig/jig.properties}</li>
 * </ol>
 */
public class JigSettingsLoader {

    public static JigSettings load(List<PartialJigSettings> highToLow) {
        Path outputDirectory = pick(highToLow, PartialJigSettings::outputDirectory)
                .orElseThrow(() -> new IllegalStateException(
                        "出力先ディレクトリが指定されていません。jig.output.directory もしくは呼び出し側で必ず指定してください。"));
        List<JigDocument> docs = highToLow.stream()
                .map(PartialJigSettings::documentTypes)
                .filter(list -> !list.isEmpty())
                .findFirst()
                .orElse(JigDocument.canonical());
        return new JigSettings(
                outputDirectory,
                pick(highToLow, PartialJigSettings::domainPattern),
                docs,
                pick(highToLow, PartialJigSettings::locale).orElse(Locale.JAPANESE)
        );
    }

    public static JigSettings loadStandard(PartialJigSettings explicit) {
        return loadStandard(explicit, PartialJigSettings.EMPTY);
    }

    /**
     * 標準優先順位での読み込みに、最低優先度の {@code fallback} 層を追加する。
     *
     * 出力先ディレクトリのように jig-core 側にデフォルトを持たないフィールドを、
     * 呼び出し側（CLI / Gradle plugin）固有の既定値で補完するための層。
     */
    public static JigSettings loadStandard(PartialJigSettings explicit, PartialJigSettings fallback) {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path userHomeJig = Paths.get(System.getProperty("user.home")).resolve(".jig");
        return load(List.of(
                explicit,
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
