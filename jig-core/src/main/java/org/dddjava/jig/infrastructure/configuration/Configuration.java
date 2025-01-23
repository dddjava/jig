package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.CommentRepository;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.data.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
import org.dddjava.jig.infrastructure.filesystem.LocalClassFileSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCommentRepository;

public class Configuration {
    JigProperties properties;

    JigSourceReader jigSourceReader;
    JigDocumentGenerator jigDocumentGenerator;
    JigService jigService;
    JigDocumentContext jigDocumentContext;

    public Configuration(JigProperties jigProperties) {
        this.properties = new JigPropertyLoader(jigProperties).load();

        CommentRepository commentRepository = new OnMemoryCommentRepository();

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        this.jigService = new JigService(architecture);

        this.jigSourceReader = new JigSourceReader(
                commentRepository,
                new AsmFactReader(),
                new JavaparserReader(properties),
                new MyBatisSqlReader(),
                new LocalClassFileSourceReader()
        );

        this.jigDocumentContext = new JigDocumentContextImpl(commentRepository, properties);
        this.jigDocumentGenerator = new JigDocumentGenerator(jigDocumentContext, jigService);
    }

    public JigSourceReader sourceReader() {
        return jigSourceReader;
    }

    public JigDocumentGenerator documentGenerator() {
        return jigDocumentGenerator;
    }

}
