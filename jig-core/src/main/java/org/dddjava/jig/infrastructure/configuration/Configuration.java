package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceCollector;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryBuilder;
import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

public class Configuration {
    JigProperties properties;

    DefaultJigRepositoryBuilder defaultJigRepositoryBuilder;
    JigDocumentGenerator jigDocumentGenerator;
    JigService jigService;
    JigDocumentContext jigDocumentContext;

    public Configuration(JigProperties jigProperties) {
        this.properties = new JigPropertyLoader(jigProperties).load();

        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        JigEventRepository jigEventRepository = new JigEventRepository();
        this.jigService = new JigService(architecture, jigEventRepository);

        this.defaultJigRepositoryBuilder = new DefaultJigRepositoryBuilder(
                glossaryRepository,
                new JavaparserReader(properties),
                new MyBatisMyBatisStatementsReader(),
                new ClassOrJavaSourceCollector(),
                jigEventRepository
        );

        this.jigDocumentContext = new JigDocumentContextImpl(glossaryRepository, properties);
        this.jigDocumentGenerator = new JigDocumentGenerator(jigDocumentContext, jigService);
    }

    public DefaultJigRepositoryBuilder sourceReader() {
        return defaultJigRepositoryBuilder;
    }

    public JigDocumentGenerator documentGenerator() {
        return jigDocumentGenerator;
    }

}
