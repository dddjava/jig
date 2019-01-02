package testing;

import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.configuration.ConfigurationContext;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigTestExtension implements ParameterResolver {

    public final Configuration configuration;

    public JigTestExtension() {
        configuration = new Configuration(
                new JigProperties(
                        new BusinessRuleCondition("stub.domain.model.+"),
                        new OutputOmitPrefix(),
                        new PackageDepth(),
                        false
                ),
                new ConfigurationContext() {
                    @Override
                    public String classFileDetectionWarningMessage() {
                        return "";
                    }

                    @Override
                    public String modelDetectionWarningMessage() {
                        return "";
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == Configuration.class) return true;
        if (parameterContext.getParameter().getType() == Configuration.class) return true;
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
        throw new AssertionError();
    }
}
