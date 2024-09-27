package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 実行時に設定するプロパティ。
 */
public class JigProperties {
    static Logger logger = LoggerFactory.getLogger(JigProperties.class);

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
     * 出力対象となるJigDocumentのリスト。
     * 全部出ると邪魔／時間がかかる時に指定。
     */
    List<JigDocument> jigDocuments;

    /**
     * 実験的: 図のノードからリンクする場合に使用。
     * SVGの限定かつうまく使うのが難しいのでなくすかもしれない。
     */
    LinkPrefix linkPrefix;

    /**
     * 最小のコンストラクタ。あまり変更しない。
     */
    public JigProperties(List<JigDocument> jigDocuments, String domainPattern, Path outputDirectory) {
        this(
                jigDocuments,
                domainPattern,
                outputDirectory,
                JigDiagramFormat.valueOf(JigProperty.OUTPUT_DIAGRAM_FORMAT.defaultValue()),
                LinkPrefix.disable()
        );
    }

    /**
     * 実験的な項目も含むコンストラクタ。よく変わる。
     */
    public JigProperties(List<JigDocument> jigDocuments,
                         String domainPattern,
                         Path outputDirectory,
                         JigDiagramFormat outputDiagramFormat,
                         LinkPrefix linkPrefix) {
        this.jigDocuments = jigDocuments;

        this.domainPattern = domainPattern;
        this.linkPrefix = linkPrefix;

        this.outputDirectory = outputDirectory;
        this.outputDiagramFormat = outputDiagramFormat;
    }

    static JigProperties defaultInstance() {
        return new JigProperties(JigDocument.canonical(), JigProperty.PATTERN_DOMAIN.defaultValue(), Paths.get(JigProperty.OUTPUT_DIRECTORY.defaultValue()));
    }

    public String getDomainPattern() {
        return domainPattern;
    }

    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }

    public Path resolveOutputPath(DocumentName documentName) {
        return outputDirectory.resolve(outputPath(documentName, outputDiagramFormat)).toAbsolutePath();
    }

    private String outputPath(DocumentName documentName, JigDiagramFormat JigDiagramFormat) {
        return documentName.fileName() + '.' + JigDiagramFormat.extension();
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
                ", linkPrefix=" + linkPrefix +
                ", outputDirectory=" + outputDirectory +
                ", outputDiagramFormat=" + outputDiagramFormat +
                '}';
    }
}
