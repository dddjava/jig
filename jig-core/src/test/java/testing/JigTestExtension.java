package testing;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.TextSourceLocations;
import org.dddjava.jig.infrastructure.LocalFileRawSourceFactory;
import org.dddjava.jig.infrastructure.codeparser.SourceCodeJapaneseReader;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Collections;

public class JigTestExtension implements ParameterResolver {

    public final Configuration configuration;

    public JigTestExtension() {
        configuration = new Configuration(
                new JigProperties(
                        new BusinessRuleCondition("stub.domain.model.+"),
                        new OutputOmitPrefix()
                ),
                new SourceCodeJapaneseReader(Collections.singletonList(new JavaparserAliasReader()))
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Configuration.class) return true;
        if (parameterContext.getParameter().getType() == RawSource.class) return true;
        if (parameterContext.getParameter().getType() == AnalyzedImplementation.class) return true;
        for (Field field : Configuration.class.getDeclaredFields()) {
            if (field.getType() == parameterContext.getParameter().getType()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Configuration.class) return configuration;
        if (parameterContext.getParameter().getType() == RawSource.class) return getTestRawSource();
        if (parameterContext.getParameter().getType() == AnalyzedImplementation.class) return getAnalyzedImplementation();
        for (Field field : Configuration.class.getDeclaredFields()) {
            if (field.getType() == parameterContext.getParameter().getType()) {
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

    private AnalyzedImplementation getAnalyzedImplementation() {
        ImplementationService implementationService = configuration.implementationService();
        return implementationService.implementations(getRawSourceLocations());
    }

    public RawSource getTestRawSource() {
        RawSourceLocations rawSourceLocations = getRawSourceLocations();
        LocalFileRawSourceFactory localFileRawSourceFactory = new LocalFileRawSourceFactory();
        return localFileRawSourceFactory.createSource(rawSourceLocations);
    }

    public RawSourceLocations getRawSourceLocations() {
        return new RawSourceLocations(
                new BinarySourceLocations(Collections.singletonList(Paths.get(TestSupport.defaultPackageClassURI()))),
                new TextSourceLocations(Collections.singletonList(TestSupport.getModuleRootPath().resolve("src").resolve("test").resolve("java")))
        );
    }
}
