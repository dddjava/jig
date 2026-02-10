package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * 実行時に設定するプロパティ。
 */
public class JigProperties {
    private static final Logger logger = LoggerFactory.getLogger(JigProperties.class);

    /**
     * 主要: ドメイン（主として扱うクラス名）のパターン
     */
    String domainPattern;
    /**
     * 主要: ドキュメントの出力先ディレクトリ
     */
    Path outputDirectory;

    /**
     * 図の出力形式
     */
    JigDiagramFormat outputDiagramFormat;

    /**
     * dotコマンドのタイムアウト
     */
    Duration outputDotTimeout;

    /**
     * 出力対象となるJigDocumentのリスト。
     * 全部出ると邪魔／時間がかかる時に指定。
     */
    List<JigDocument> jigDocuments;

    /**
     * ダイアグラムで関係を簡略化して出力する
     *
     * パッケージ関連図とビジネスルール関連図が対象です。
     */
    public boolean diagramTransitiveReduction;

    /**
     * 最小のコンストラクタ。あまり変更しない。
     */
    public JigProperties(List<JigDocument> jigDocuments, String domainPattern, Path outputDirectory) {
        this(
                jigDocuments,
                domainPattern,
                outputDirectory,
                JigDiagramFormat.valueOf(JigProperty.OUTPUT_DIAGRAM_FORMAT.defaultValue()),
                true,
                Duration.ofSeconds(10)
        );
    }

    /**
     * 実験的な項目も含むコンストラクタ。よく変わる。
     */
    public JigProperties(List<JigDocument> jigDocuments,
                         String domainPattern,
                         Path outputDirectory,
                         JigDiagramFormat outputDiagramFormat,
                         boolean diagramTransitiveReduction,
                         Duration outputDotTimeout) {
        this.jigDocuments = jigDocuments;

        this.domainPattern = domainPattern;

        this.outputDirectory = outputDirectory;
        this.outputDiagramFormat = outputDiagramFormat;
        this.outputDotTimeout = outputDotTimeout;
        this.diagramTransitiveReduction = diagramTransitiveReduction;
    }

    static JigProperties defaultInstance() {
        return new JigProperties(JigDocument.canonical(), JigProperty.PATTERN_DOMAIN.defaultValue(), Paths.get(JigProperty.OUTPUT_DIRECTORY.defaultValue()));
    }

    public String getDomainPattern() {
        return domainPattern;
    }

    public void override(JigProperties overrideProperties) {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                Object currentValue = field.get(this);
                // nullでないフィールドは全て上書きする
                Object value = field.get(overrideProperties);
                if (value != null) {
                    if (value.equals(currentValue)) continue;
                    if (value.equals("")) continue;

                    field.set(this, value);
                    logger.info("configure {} from {} to {}", field.getName(), currentValue, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "JigProperties{" +
                "businessRulePattern='" + domainPattern + '\'' +
                ", outputDirectory=" + outputDirectory +
                ", outputDiagramFormat=" + outputDiagramFormat +
                '}';
    }
}
