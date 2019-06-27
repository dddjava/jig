package testing;

import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.SourceCodeAliasReader;
import org.dddjava.jig.domain.model.implementation.source.SourcePaths;
import org.dddjava.jig.domain.model.implementation.source.Sources;
import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourcePaths;
import org.dddjava.jig.domain.model.implementation.source.code.CodeSourcePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
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
                new SourceCodeAliasReader(new JavaparserAliasReader())
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Configuration.class) return true;
        if (parameterContext.getParameter().getType() == Sources.class) return true;
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
        if (parameterContext.getParameter().getType() == Sources.class) return getTestRawSource();
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

    public Sources getTestRawSource() {
        SourcePaths sourcePaths = getRawSourceLocations();
        LocalFileSourceReader localFileRawSourceFactory = new LocalFileSourceReader();
        return localFileRawSourceFactory.readSources(sourcePaths);
    }

    public SourcePaths getRawSourceLocations() {
        return new SourcePaths(
                new BinarySourcePaths(Collections.singletonList(Paths.get(TestSupport.defaultPackageClassURI()))),
                new CodeSourcePaths(Collections.singletonList(TestSupport.getModuleRootPath().resolve("src").resolve("test").resolve("java")))
        );
    }
}
