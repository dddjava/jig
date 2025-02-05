package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.*;
import org.dddjava.jig.domain.model.data.Architecture;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceCollector;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

public class Configuration {
    JigProperties properties;

    JigSourceReader jigSourceReader;
    JigDocumentGenerator jigDocumentGenerator;
    JigService jigService;
    JigDocumentContext jigDocumentContext;

    public Configuration(JigProperties jigProperties) {
        this.properties = new JigPropertyLoader(jigProperties).load();

        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        JigEventRepository jigEventRepository = new JigEventRepository();
        this.jigService = new JigService(architecture, jigEventRepository);

        this.jigSourceReader = new JigSourceReader(
                glossaryRepository,
                new AsmClassSourceReader(),
                new JavaparserReader(properties),
                new MyBatisMyBatisStatementsReader(),
                new ClassOrJavaSourceCollector(),
                jigEventRepository
        );

        this.jigDocumentContext = new JigDocumentContextImpl(glossaryRepository, properties);
        this.jigDocumentGenerator = new JigDocumentGenerator(jigDocumentContext, jigService);
    }

    public JigSourceReader sourceReader() {
        return jigSourceReader;
    }

    public JigDocumentGenerator documentGenerator() {
        return jigDocumentGenerator;
    }

}
