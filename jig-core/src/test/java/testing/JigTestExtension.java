package testing;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.sources.JigRepository;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.filesystem.ClassOrJavaSourceCollector;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class JigTestExtension implements ParameterResolver {

    public final Configuration configuration;

    public JigTestExtension() throws Exception {
        Path tempDir = Files.createTempDirectory("jig");
        configuration = new Configuration(
                new JigProperties(JigDocument.canonical(), "stub.domain.model.+", tempDir));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        if (parameterType == Configuration.class
                || parameterType == SourceBasePaths.class
                || parameterType == JigRepository.class) {
            return true;
        }

        for (Field field : Configuration.class.getDeclaredFields()) {
            if (field.getType() == parameterType) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        if (parameterType == Configuration.class) return configuration;
        if (parameterType == SourceBasePaths.class) return TestSupport.getRawSourceLocations();
        if (parameterType == JigRepository.class) {
            DefaultJigRepositoryFactory factory = DefaultJigRepositoryFactory.init(configuration);
            factory.readPathSource(TestSupport.getRawSourceLocations());
            return factory.jigTypesRepository();
        }

        for (Field field : Configuration.class.getDeclaredFields()) {
            if (field.getType() == parameterType) {
                try {
                    field.setAccessible(true);
                    return field.get(configuration);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }
        }

        // 実装ミスでもなければここには来ない
        throw new AssertionError();
    }

    public Sources getTestRawSource() {
        SourceBasePaths sourceBasePaths = TestSupport.getRawSourceLocations();
        ClassOrJavaSourceCollector localFileRawSourceFactory = new ClassOrJavaSourceCollector();
        return localFileRawSourceFactory.collectSources(sourceBasePaths);
    }
}
