package testing;

import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.nio.file.Files;
import java.nio.file.Path;

public class JigTestExtension implements ParameterResolver {

    public final Configuration configuration;

    public JigTestExtension() throws Exception {
        Path tempDir = Files.createTempDirectory("jig");
        configuration = Configuration.from(
                new JigProperties(JigDocument.canonical(), "stub.domain.model.+", tempDir));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType == Configuration.class
                || parameterType == SourceBasePaths.class
                || parameterType == JigRepository.class
                || parameterType == GlossaryRepository.class
                || parameterType == JigEventRepository.class
                || parameterType == JigProperties.class
                || parameterType == JigDocumentGenerator.class
                || parameterType == JigService.class
                || parameterType == JigDocumentContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        if (parameterType == Configuration.class) return configuration;
        if (parameterType == SourceBasePaths.class) return TestSupport.getRawSourceLocations();
        if (parameterType == JigRepository.class) {
            DefaultJigRepositoryFactory factory = DefaultJigRepositoryFactory.init(configuration);
            return factory.createJigRepository(TestSupport.getRawSourceLocations());
        }
        if (parameterType == GlossaryRepository.class) return configuration.glossaryRepository();
        if (parameterType == JigEventRepository.class) return configuration.jigEventRepository();
        if (parameterType == JigProperties.class) return configuration.properties();
        if (parameterType == JigDocumentGenerator.class) return configuration.jigDocumentGenerator();
        if (parameterType == JigService.class) return configuration.jigService();
        if (parameterType == JigDocumentContext.class) return configuration.jigDocumentContext();

        // 実装ミスでもなければここには来ない
        throw new AssertionError();
    }
}
