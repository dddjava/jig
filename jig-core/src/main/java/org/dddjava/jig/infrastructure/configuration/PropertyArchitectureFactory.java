package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureFactory;
import org.dddjava.jig.domain.model.jigmodel.architecture.IsBusinessRule;

import java.util.regex.Pattern;

public class PropertyArchitectureFactory implements ArchitectureFactory {

    JigProperties jigProperties;

    public PropertyArchitectureFactory(JigProperties jigProperties) {
        this.jigProperties = jigProperties;
    }

    @Override
    public Architecture architecture() {
        Pattern compilerGeneratedClassPattern = Pattern.compile(".+\\$\\d+");
        Pattern businessRulePattern = Pattern.compile(jigProperties.getBusinessRulePattern());
        IsBusinessRule isBusinessRule = typeByteCode -> {
            String fqn = typeByteCode.typeIdentifier().fullQualifiedName();
            return businessRulePattern.matcher(fqn).matches()
                    && !compilerGeneratedClassPattern.matcher(fqn).matches();
        };
        return new Architecture(isBusinessRule);
    }
}
