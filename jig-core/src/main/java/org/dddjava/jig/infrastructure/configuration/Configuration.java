package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.*;
import org.dddjava.jig.domain.model.data.Architecture;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;
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

        JigReporter jigReporter = new JigReporter();
        this.jigService = new JigService(architecture, jigReporter);

        this.jigSourceReader = new JigSourceReader(
                commentRepository,
                new AsmClassSourceReader(),
                new JavaparserReader(properties),
                new MyBatisMyBatisStatementsReader(),
                new ClassOrJavaSourceReader(),
                jigReporter
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
