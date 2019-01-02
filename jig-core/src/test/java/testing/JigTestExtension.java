package testing;

import org.dddjava.jig.domain.model.implementation.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.raw.BinarySourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.dddjava.jig.domain.model.implementation.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.TextSourceLocations;
import org.dddjava.jig.infrastructure.LocalFileRawSourceFactory;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
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
                        new OutputOmitPrefix(),
                        new PackageDepth(),
                        false
                )
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Configuration.class) return true;
        if (parameterContext.getParameter().getType() == RawSource.class) return true;
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

    public RawSource getTestRawSource() {
        RawSourceLocations rawSourceLocations = new RawSourceLocations(
                new BinarySourceLocations(Collections.singletonList(Paths.get(TestSupport.defaultPackageClassURI()))),
                new TextSourceLocations(Collections.singletonList(TestSupport.getModuleRootPath().resolve("src").resolve("test").resolve("java")))
        );

        LocalFileRawSourceFactory localFileRawSourceFactory = new LocalFileRawSourceFactory();
        return localFileRawSourceFactory.createSource(rawSourceLocations);
    }
}
