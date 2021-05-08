package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 実行時に設定するプロパティ。
 */
public class JigProperties {
    /** 主要: ドメイン（主として扱うクラス名）のパターン */
    String domainPattern;
    /** 主要: ドキュメントの出力先ディレクトリ */
    Path outputDirectory;

    /** 図の出力形式 */
    JigDiagramFormat outputDiagramFormat;

    /**
     * 出力対象となるJigDocumentのリスト。
     * 全部出ると邪魔／時間がかかる時に指定。
     */
    List<JigDocument> jigDocuments;

    /**
     * 図の出力時に出力を抑止する冗長な部分。
     * TODO 廃止予定。指定しなくても導出できると思われる。
     */
    OutputOmitPrefix outputOmitPrefix;

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
                new OutputOmitPrefix(JigProperty.OMIT_PREFIX.defaultValue()),
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
                         OutputOmitPrefix outputOmitPrefix,
                         LinkPrefix linkPrefix) {
        this.jigDocuments = jigDocuments;
        this.outputOmitPrefix = outputOmitPrefix;

        this.domainPattern = domainPattern;
        this.linkPrefix = linkPrefix;

        this.outputDirectory = outputDirectory;
        this.outputDiagramFormat = outputDiagramFormat;
    }

    static JigProperties defaultInstance() {
        return new JigProperties(JigDocument.canonical(), JigProperty.PATTERN_DOMAIN.defaultValue(), Paths.get(JigProperty.OUTPUT_DIRECTORY.defaultValue()));
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
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

    void prepareOutputDirectory() {
        File file = outputDirectory.toFile();
        if (file.exists()) {
            if (file.isDirectory() && file.canWrite()) {
                // ディレクトリかつ書き込み可能なので対応不要
                return;
            }
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() + " is not Directory. Please review your settings.");
            }
            if (file.isDirectory() && !file.canWrite()) {
                throw new IllegalStateException(file.getAbsolutePath() + " can not writable. Please specify another directory.");
            }
        }

        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void override(JigProperties jigProperties) {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                // nullでないフィールドは全て上書きする
                Object value = field.get(jigProperties);
                if (value != null) {
                    if (value instanceof String) {
                        if (!((String) value).isEmpty()) {
                            field.set(this, value);
                        }
                    } else {
                        field.set(this, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "JigProperties{" +
                "outputOmitPrefix=" + outputOmitPrefix +
                ", businessRulePattern='" + domainPattern + '\'' +
                ", linkPrefix=" + linkPrefix +
                ", outputDirectory=" + outputDirectory +
                ", outputDiagramFormat=" + outputDiagramFormat +
                '}';
    }
}
