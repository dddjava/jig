package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.*;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.sources.jigreader.AdditionalTextSourceReader;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceReader;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
import org.dddjava.jig.infrastructure.filesystem.LocalClassFileSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCommentRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJigSourceRepository;

public class Configuration {
    JigProperties properties;

    JigSourceReader jigSourceReader;
    JigDocumentGenerator jigDocumentGenerator;
    JigService jigService;
    JigDocumentContext jigDocumentContext;

    public Configuration(JigProperties jigProperties) {
        this(jigProperties, new AdditionalTextSourceReader());
    }

    public Configuration(JigProperties jigProperties, AdditionalTextSourceReader additionalTextSourceReader) {
        this.properties = new JigPropertyLoader(jigProperties).load();

        CommentRepository commentRepository = new OnMemoryCommentRepository();

        JigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(commentRepository);

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        this.jigService = new JigService(architecture, jigSourceRepository);

        JavaparserReader javaparserReader = new JavaparserReader(properties);
        TextSourceReader textSourceReader = new TextSourceReader(javaparserReader, additionalTextSourceReader);

        this.jigSourceReader = new JigSourceReader(
                jigSourceRepository,
                new AsmFactReader(),
                textSourceReader,
                new MyBatisSqlReader(),
                new LocalClassFileSourceReader()
        );

        jigDocumentContext = new JigDocumentContextImpl(commentRepository, properties);
        this.jigDocumentGenerator = JigDocumentGenerator.from(jigDocumentContext, jigService);
    }

    public JigSourceReader sourceReader() {
        return jigSourceReader;
    }

    public JigDocumentGenerator documentGenerator() {
        return jigDocumentGenerator;
    }

}
