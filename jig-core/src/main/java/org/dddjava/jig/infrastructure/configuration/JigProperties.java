package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigProperties {

    OutputOmitPrefix outputOmitPrefix;
    String businessRulePattern;

    String applicationPattern;
    String infrastructurePattern;
    String presentationPattern;

    LinkPrefix linkPrefix;

    Path outputDirectory;
    JigDiagramFormat outputDiagramFormat;

    public JigProperties(String businessRulePattern, String applicationPattern, String infrastructurePattern, String presentationPattern, OutputOmitPrefix outputOmitPrefix, LinkPrefix linkPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;

        this.businessRulePattern = businessRulePattern;
        this.presentationPattern = presentationPattern;
        this.applicationPattern = applicationPattern;
        this.infrastructurePattern = infrastructurePattern;

        this.linkPrefix = linkPrefix;

    }

    public JigProperties(OutputOmitPrefix outputOmitPrefix, String businessRulePattern, String applicationPattern, String infrastructurePattern, String presentationPattern, LinkPrefix linkPrefix, Path outputDirectory, JigDiagramFormat outputDiagramFormat) {
        this.outputOmitPrefix = outputOmitPrefix;

        this.businessRulePattern = businessRulePattern;
        this.applicationPattern = applicationPattern;
        this.infrastructurePattern = infrastructurePattern;
        this.presentationPattern = presentationPattern;

        this.linkPrefix = linkPrefix;

        this.outputDirectory = outputDirectory;
        this.outputDiagramFormat = outputDiagramFormat;
    }

    public static JigProperties defaultInstance() {
        JigProperties jigProperties = new JigProperties(
                new OutputOmitPrefix(JigProperty.OMIT_PREFIX.defaultValue()),
                JigProperty.PATTERN_DOMAIN.defaultValue(),
                JigProperty.PATTERN_APPLICATION.defaultValue(),
                JigProperty.PATTERN_INFRASTRUCTURE.defaultValue(),
                JigProperty.PATTERN_PRESENTATION.defaultValue(),
                LinkPrefix.disable(),
                Paths.get(JigProperty.OUTPUT_DIRECTORY.defaultValue()),
                JigDiagramFormat.valueOf(JigProperty.OUTPUT_DIAGRAM_FORMAT.defaultValue())
        );
        return jigProperties;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public String getBusinessRulePattern() {
        return businessRulePattern;
    }

    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }

    public String getInfrastructurePattern() {
        return infrastructurePattern;
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
                ", businessRulePattern='" + businessRulePattern + '\'' +
                ", applicationPattern='" + applicationPattern + '\'' +
                ", infrastructurePattern='" + infrastructurePattern + '\'' +
                ", presentationPattern='" + presentationPattern + '\'' +
                ", linkPrefix=" + linkPrefix +
                ", outputDirectory=" + outputDirectory +
                ", outputDiagramFormat=" + outputDiagramFormat +
                '}';
    }
}
