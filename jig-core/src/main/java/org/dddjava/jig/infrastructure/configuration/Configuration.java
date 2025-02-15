package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

public class Configuration {
    private final GlossaryRepository glossaryRepository;
    private final JigEventRepository jigEventRepository;
    JigProperties properties;

    DefaultJigRepositoryFactory defaultJigRepositoryFactory;
    JigDocumentGenerator jigDocumentGenerator;
    JigService jigService;
    JigDocumentContext jigDocumentContext;

    public Configuration(JigProperties jigProperties) {
        this.properties = new JigPropertyLoader(jigProperties).load();

        glossaryRepository = new OnMemoryGlossaryRepository();

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        jigEventRepository = new JigEventRepository();
        jigService = new JigService(architecture, jigEventRepository);

        jigDocumentContext = new JigDocumentContextImpl(glossaryRepository, properties);
        jigDocumentGenerator = new JigDocumentGenerator(jigDocumentContext, jigService);
    }

    public DefaultJigRepositoryFactory sourceReader() {
        return defaultJigRepositoryFactory;
    }

    public JigDocumentGenerator documentGenerator() {
        return jigDocumentGenerator;
    }

    public GlossaryRepository glossaryRepository() {
        return glossaryRepository;
    }

    public JigEventRepository jigEventRepository() {
        return jigEventRepository;
    }
}
